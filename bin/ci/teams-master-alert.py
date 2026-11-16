#!/usr/bin/env python3
"""Send fines service master build results to the two Teams channels.

Jenkins posts a GitHub commit status when a build starts and finishes. The GitHub
Action passes that status to this script. We accept only the regular fines service
``master`` job; PR, nightly and environment jobs are ignored.

The Builds channel receives every completed build. With REPORT_ALL_BUILDS=true,
the Alerts channel also receives every completed build. Otherwise it receives
the first RED build, a new-code build that remains RED, and GREEN recovery.
Pending builds never send a message. Compare build SHAs to distinguish new code
from a repeated build; compare completed results to identify recovery. Neither
comparison proves why a build failed or whether a merge was appropriate.
"""

import json
import os
import re
import subprocess
import sys

from teams_card import commit_action, jenkins_actions, make_card, send_card


REPO = "opal-fines-service"
BASE = f"repos/hmcts/{REPO}"
MASTER_JOB = f"https://sds-build.hmcts.net/job/HMCTS/job/{REPO}/job/master/"
MASTER_BUILD = re.compile(
    rf"^{re.escape(MASTER_JOB)}(\d+)(?:/|$)"
)
# Keys: (failed, previous completed result was RED, previous numbered build has same SHA).
RESULT_TEXT = {
    (False, False, True): "Rebuild remains GREEN.",
    (False, False, False): "Master remains GREEN after new code was built.",
    (False, True, True): "Rebuild is GREEN; master has recovered.",
    (False, True, False): "Master is GREEN again after new code was built.",
    (True, False, True): "Master has turned RED on unchanged code.",
    (True, False, False): "Master has turned RED after new code was built.",
    (True, True, True): "Rebuild remains RED.",
    (True, True, False): "Master remains RED after new code was built.",
}


def is_aborted(build):
    """Jenkins records an aborted build as a GitHub error with a description."""
    return build["state"] == "error" and "aborted" in (build.get("description") or "").lower()


def api(path):
    """Read GitHub's JSON API using the token supplied by the Action."""
    result = subprocess.run(["gh", "api", path], check=True, capture_output=True, text=True)
    return json.loads(result.stdout)


def build_history(ref, before=None, completed_count=2):
    """Collect master statuses by build number across up to 40 first parents."""
    commit = api(f"{BASE}/commits/master") if ref == "master" else {"sha": ref}
    builds = {}
    for _ in range(40):
        sha = commit["sha"]
        for status in api(f"{BASE}/commits/{sha}/statuses?per_page=100"):
            match = MASTER_BUILD.match(status.get("target_url") or "")
            if not match or status.get("context") != "continuous-integration/jenkins/branch":
                continue
            number = int(match.group(1))
            if before is not None and number >= before:
                continue
            if number not in builds or status["id"] > builds[number]["id"]:
                builds[number] = {**status, "sha": sha}
        ordered = [builds[n] for n in sorted(builds, reverse=True)]
        completed = [build for build in ordered if build["state"] in ("success", "failure", "error")]
        if len(completed) >= completed_count:
            return ordered
        if "parents" not in commit:
            commit = api(f"{BASE}/commits/{sha}")
        if not commit["parents"]:
            return ordered
        commit = api(f'{BASE}/commits/{commit["parents"][0]["sha"]}')
    return ordered


def completed_builds():
    """Find the latest two completed regular master builds."""
    return [build for build in build_history("master")
            if build["state"] in ("success", "failure", "error")][:2]


def status_for_event(sha, event_id, event_url):
    """Confirm this is the final status for that build.

    Jenkins can post multiple pending statuses before a final result. The event
    ID must match the latest status for its build, so pending or superseded
    events do not produce duplicate cards.
    """
    statuses = [status for status in api(f"{BASE}/commits/{sha}/statuses?per_page=100")
                if status.get("target_url") == event_url
                and status.get("context") == "continuous-integration/jenkins/branch"]
    latest = max(statuses, key=lambda status: status["id"], default=None)
    if latest and latest["id"] == event_id and latest["state"] in ("success", "failure", "error"):
        return latest
    return None


def previous_builds(sha, number):
    """Find the preceding numbered build and preceding completed result.

    Both must have a lower build number than the event. The preceding build
    could still be pending, whereas a result comparison needs a completed one.
    Follow first parents so a new merge commit can see earlier master builds.
    """
    ordered = build_history(sha, before=number, completed_count=1)
    completed = next((build for build in ordered
                      if build["state"] in ("success", "failure", "error")), None)
    return (ordered[0] if ordered else None), completed


def message_for(build, previous_numbered, previous_completed, report_all=False):
    """Describe the result transition and whether the built code changed.

    An unknown earlier result is not called GREEN, and a successful rebuild is
    not described as a confirmed fix to the underlying problem.
    """
    if is_aborted(build):
        return ("Master build was ABORTED; no completed result is available. "
                "Check master before routine merges and decide whether to rerun the build.")
    if previous_completed and is_aborted(previous_completed):
        state = "GREEN" if build["state"] == "success" else "RED"
        instruction = ("Check the latest master result before routine merges." if state == "GREEN" else
                       "Check master before routine merges and coordinate investigation.")
        return f"Master is {state} following an aborted build. {instruction}"
    failed = build["state"] in ("failure", "error")
    was_red = previous_completed and previous_completed["state"] in ("failure", "error")
    new_code_since_result = previous_completed and build["sha"] != previous_completed["sha"]
    if not report_all and (failed and was_red and not new_code_since_result or
                           not failed and not was_red):
        return None
    same_code = previous_numbered and build["sha"] == previous_numbered["sha"]
    if failed:
        state = "remains RED" if was_red else "has turned RED"
    else:
        state = "is GREEN again" if was_red else "remains GREEN"
    if not previous_completed:
        result = f"Master build is {'RED' if failed else 'GREEN'}; the previous result is unavailable."
    elif not previous_numbered:
        result = f"Master {state}; the previous build is unavailable."
    elif previous_numbered["state"] == "pending":
        if not failed and not was_red:
            state = "is GREEN"
        if failed and new_code_since_result and not same_code:
            result = f"Master {state} after new code was built; the preceding build has no final GitHub status."
        else:
            result = f"Master {state}; the preceding build has no final GitHub status."
    else:
        result = RESULT_TEXT[(failed, bool(was_red), bool(same_code))]
    instruction = ("Check master before routine merges and coordinate investigation." if failed else
                   "Check the latest master result before routine merges.")
    return f"{result} {instruction}"


def merged_pr_for_commit(sha):
    """Return a PR only when GitHub confirms this SHA is its master merge commit.

    The association endpoint can also return PRs containing an earlier commit.
    Such associations must not be presented as a merge that triggered this build.
    PR metadata is optional; an API error must not suppress the build alert.
    """
    try:
        associated = api(f"{BASE}/commits/{sha}/pulls?per_page=100")
        merged = [pr for pr in associated if pr.get("merged_at")
                  and pr.get("merge_commit_sha") == sha
                  and pr.get("base", {}).get("ref") == "master"]
        if len(merged) != 1:
            return None
        return api(f'{BASE}/pulls/{merged[0]["number"]}')
    except subprocess.CalledProcessError:
        print("PR metadata unavailable; sending build result without attribution.", file=sys.stderr)
        return None


def card_for(message, build, merged_pr=None, new_code=False):
    """Build a Teams card with stable Jenkins destinations and status styling.

    Teams chooses the actual shades for Good and Attention in each theme. The
    source status URL may point to a stage view, so links use canonical pages.
    """
    number = MASTER_BUILD.match(build["target_url"]).group(1)
    failed = build["state"] in ("failure", "error")
    status = "ABORTED" if is_aborted(build) else build["state"].upper()
    actions = jenkins_actions(MASTER_JOB, number, "Latest master", console=failed)
    details = []
    if merged_pr:
        pr_number = merged_pr["number"]
        merged_by = (merged_pr.get("merged_by") or {}).get("login")
        description = f"Merged PR #{pr_number}"
        if merged_by:
            description += f" by @{merged_by}"
        details.append({"type": "TextBlock", "text": description + ".", "wrap": True,
                        "size": "Small", "spacing": "Small"})
        actions.append({"type": "Action.OpenUrl", "title": f"PR #{pr_number}",
                        "url": merged_pr["html_url"]})
    elif new_code:
        short_sha = build["sha"][:7]
        details.append({"type": "TextBlock", "text": f"Commit {short_sha}; no merged PR verified.",
                        "wrap": True, "size": "Small", "spacing": "Small"})
        actions.append(commit_action(REPO, build["sha"]))
    heading = f"{'🔴' if failed else '🟢'} Fines Service master · #{number} · {status}"
    return make_card(heading, message, "attention" if failed else "good", actions, details)


def main():
    """Select the status, find earlier builds, then send its Adaptive Card."""
    event_id = int(os.environ["STATUS_ID"])
    event_url = os.environ.get("STATUS_URL", "")
    report_all_setting = (os.environ.get("REPORT_ALL_BUILDS") or "").strip().lower()
    if report_all_setting not in ("true", "false", ""):
        print("Invalid OPAL_MASTER_ALERT_REPORT_ALL_BUILDS; using false.", file=sys.stderr)
    report_all_alerts = report_all_setting == "true"
    if not MASTER_BUILD.match(event_url):
        print("Ignoring a non-master Jenkins status.")
        return
    sha = os.environ["STATUS_SHA"]
    current = status_for_event(sha, event_id, event_url)
    if not current:
        print("No final master build result to report.")
        return
    current = {**current, "sha": sha}
    number = int(MASTER_BUILD.match(event_url).group(1))
    previous_numbered, previous_completed = previous_builds(sha, number)
    message = message_for(current, previous_numbered, previous_completed, report_all=True)
    print(message)
    alerts_webhook = os.environ.get("ALERTS_WEBHOOK_URL")
    builds_webhook = os.environ.get("BUILDS_WEBHOOK_URL")
    webhooks = {"Alerts": alerts_webhook, "Builds": builds_webhook}
    # A same-commit rebuild is not another PR merge, even if the previous build
    # never published a final status and the last completed build used older code.
    new_code = previous_numbered is not None and previous_numbered["sha"] != sha
    merged_pr = merged_pr_for_commit(sha) if new_code else None
    # The webhook accepts a POST before Teams displays the card; inspect its run
    # history if delivery fails after this call succeeds.
    card = card_for(message, current, merged_pr, new_code)
    alert_message = message_for(current, previous_numbered, previous_completed, report_all_alerts)
    destinations = [("Builds", builds_webhook)]
    if alert_message:
        if report_all_alerts:
            destinations.insert(0, ("Alerts", alerts_webhook))
        else:
            builds = completed_builds()
            if builds and builds[0]["id"] == event_id and builds[0]["target_url"] == event_url:
                destinations.insert(0, ("Alerts", alerts_webhook))
            else:
                print("A newer completed master build superseded this event for Alerts.")
    failed = [name for name, webhook in webhooks.items() if not webhook]
    for name in failed:
        print(f"{name} webhook is not configured.", file=sys.stderr)
    for name, webhook in destinations:
        if not webhook:
            continue
        try:
            send_card(card, webhook)
            print(f"Sent to {name} channel.")
        except subprocess.CalledProcessError:
            print(f"Failed to send to {name} channel.", file=sys.stderr)
            failed.append(name)
    if failed:
        sys.exit(f"Teams delivery failed for: {', '.join(failed)}.")


if __name__ == "__main__":
    main()

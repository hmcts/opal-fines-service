#!/usr/bin/env python3
"""Send completed fines service nightly results to a separate Teams channel.

Jenkins dispatches the nightly result to GitHub after its pipeline finishes.
The Action accepts that event only for this repo's nightly master job. Regular
master statuses use their existing two-channel Action.
"""

import os
import re
import sys

from teams_card import commit_action, jenkins_actions, make_card, send_card


NIGHTLY_JOB = "https://sds-build.hmcts.net/job/HMCTS_Nightly/job/opal-fines-service/job/master/"
NIGHTLY_BUILD = re.compile(rf"^{re.escape(NIGHTLY_JOB)}(\d+)(?:/|$)")
RESULT_MESSAGE = {
    "SUCCESS": "Nightly build succeeded.",
    "FAILURE": "Nightly build failed.",
    "UNSTABLE": "Nightly build was unstable.",
    "ABORTED": "Nightly build was aborted.",
    "NOT_BUILT": "Nightly build did not complete.",
}


def card_for(number, result, sha):
    """Show the nightly result and direct links to that Jenkins build."""
    colour = "good" if result == "SUCCESS" else "attention" if result == "FAILURE" else "warning"
    icon = "🟢" if result == "SUCCESS" else "🔴" if result == "FAILURE" else "🟠"
    actions = jenkins_actions(NIGHTLY_JOB, number, "Latest nightly", console=result != "SUCCESS")
    if sha:
        actions.append(commit_action("opal-fines-service", sha))
    heading = f"{icon} Fines Service nightly · #{number} · {result}"
    return make_card(heading, RESULT_MESSAGE[result], colour, actions)


def main():
    match = NIGHTLY_BUILD.match(os.environ.get("BUILD_URL", ""))
    if not match:
        sys.exit("Unexpected nightly Jenkins build URL.")
    result = os.environ.get("BUILD_RESULT")
    if result not in RESULT_MESSAGE:
        sys.exit("No completed nightly result to report.")
    webhook = os.environ.get("NIGHTLY_WEBHOOK_URL")
    if not webhook:
        sys.exit("OPAL_NIGHTLY_BUILDS_TEAMS_WEBHOOK is not configured.")
    sha = os.environ.get("BUILD_SHA", "")
    if sha and not re.fullmatch(r"[0-9a-f]{40}", sha):
        sys.exit("Nightly build SHA is invalid.")
    card = card_for(match.group(1), result, sha)
    send_card(card, webhook)


if __name__ == "__main__":
    main()

"""Shared Teams card layout, Jenkins links and webhook delivery."""

import json
import subprocess


def jenkins_actions(job_url, number, latest_title, console=False):
    """Link to the job and numbered build, with a console link when useful."""
    build_url = f"{job_url}{number}/"
    actions = [
        {"type": "Action.OpenUrl", "title": latest_title, "url": job_url},
        {"type": "Action.OpenUrl", "title": f"Build #{number}", "url": build_url},
    ]
    if console:
        actions.append({"type": "Action.OpenUrl", "title": "Console output",
                        "url": f"{build_url}console"})
    return actions


def commit_action(repo, sha):
    """Link to the commit built by Jenkins."""
    return {"type": "Action.OpenUrl", "title": f"Commit {sha[:7]}",
            "url": f"https://github.com/hmcts/{repo}/commit/{sha}"}


def make_card(heading, message, colour, actions, details=None):
    """Use the same Adaptive Card structure for master and nightly results."""
    return {
        "$schema": "http://adaptivecards.io/schemas/adaptive-card.json",
        "type": "AdaptiveCard",
        "version": "1.2",
        "body": [
            {"type": "Container", "style": colour, "items": [
                {"type": "TextBlock", "text": heading, "size": "Large", "weight": "Bolder", "wrap": True},
            ]},
            {"type": "TextBlock", "text": message, "wrap": True, "separator": True,
             "spacing": "Medium"},
        ] + (details or []),
        "actions": actions,
    }


def send_card(card, webhook):
    """Post an Adaptive Card envelope to a Teams workflow webhook."""
    payload = {"type": "message", "attachments": [
        {"contentType": "application/vnd.microsoft.card.adaptive", "content": card},
    ]}
    subprocess.run(["curl", "--fail-with-body", "--silent", "--show-error",
                    "--retry", "2", "--retry-delay", "2", "-H", "Content-Type: application/json",
                    "--data", json.dumps(payload), "--output", "/dev/null", webhook], check=True)
    print("Webhook accepted the card; check the Teams workflow run for delivery.")

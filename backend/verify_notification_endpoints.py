import requests
import json
import subprocess
import time
import sys

BASE_URL = "http://localhost:8080"

def run_sql(query):
    cmd = ["psql", "-U", "postgres", "-d", "decision_hub", "-c", query]
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    return res.stdout

def print_section(title):
    print("\n" + "="*60)
    print(f" {title}")
    print("="*60)

def assert_status(response, expected_status):
    if response.status_code != expected_status:
        print(f"Assertion failed! Expected {expected_status}, got {response.status_code}")
        print(f"Response Body: {response.text}")
        sys.exit(1)

def find_notification(headers, notif_type):
    res = requests.get(f"{BASE_URL}/api/notifications", headers=headers)
    assert_status(res, 200)
    notifications = res.json()["content"]
    for n in notifications:
        if n["type"] == notif_type:
            return n
    return None

def main():
    timestamp = int(time.time())
    
    # User Details
    owner_username = f"notif_owner_{timestamp}"
    owner_email = f"notif_owner_{timestamp}@gmail.com"
    
    usera_username = f"notif_usera_{timestamp}"
    usera_email = f"notif_usera_{timestamp}@gmail.com"
    
    userb_username = f"notif_userb_{timestamp}"
    userb_email = f"notif_userb_{timestamp}@gmail.com"
    
    password = "Password123"

    print_section("STAGING AUTHENTICATION")
    
    # Register/Login Owner
    print("Registering and logging in Owner...")
    res = requests.post(f"{BASE_URL}/api/auth/register", json={"username": owner_username, "email": owner_email, "password": password})
    assert_status(res, 200)
    res = requests.post(f"{BASE_URL}/api/auth/login", json={"email": owner_email, "password": password})
    assert_status(res, 200)
    owner_token = res.json()["token"]
    owner_headers = {"Authorization": f"Bearer {owner_token}", "Content-Type": "application/json"}

    # Register/Login User A
    print("Registering and logging in User A...")
    res = requests.post(f"{BASE_URL}/api/auth/register", json={"username": usera_username, "email": usera_email, "password": password})
    assert_status(res, 200)
    res = requests.post(f"{BASE_URL}/api/auth/login", json={"email": usera_email, "password": password})
    assert_status(res, 200)
    usera_token = res.json()["token"]
    usera_headers = {"Authorization": f"Bearer {usera_token}", "Content-Type": "application/json"}

    # Register/Login User B
    print("Registering and logging in User B...")
    res = requests.post(f"{BASE_URL}/api/auth/register", json={"username": userb_username, "email": userb_email, "password": password})
    assert_status(res, 200)
    res = requests.post(f"{BASE_URL}/api/auth/login", json={"email": userb_email, "password": password})
    assert_status(res, 200)
    userb_token = res.json()["token"]
    userb_headers = {"Authorization": f"Bearer {userb_token}", "Content-Type": "application/json"}

    print_section("INTEGRATION FLOWS & EVENT GENERATION")

    # 1. Create a PRIVATE community as Owner
    print("Owner creating PRIVATE community...")
    res = requests.post(f"{BASE_URL}/api/communities", headers=owner_headers, json={
        "name": f"Private Notif Comm {timestamp}",
        "slug": f"private-notif-comm-{timestamp}",
        "description": "Integration verification private community",
        "categoryId": 1,
        "visibility": "PRIVATE"
    })
    assert_status(res, 200)
    community_id = res.json()["id"]
    print(f"Community Created with ID: {community_id}")

    # 2. User A joins private community (Status = PENDING)
    print("User A requesting to join private community...")
    res = requests.post(f"{BASE_URL}/api/communities/{community_id}/join", headers=usera_headers)
    assert_status(res, 200)
    
    # 3. Owner gets pending requests & approves User A -> triggers MEMBERSHIP_APPROVED
    print("Owner retrieving pending requests...")
    res = requests.get(f"{BASE_URL}/api/communities/{community_id}/requests", headers=owner_headers)
    assert_status(res, 200)
    pending_list = res.json()
    usera_member_id = None
    for req in pending_list:
        if req["username"] == usera_username:
            usera_member_id = req["memberId"]
            break
    
    assert usera_member_id is not None, "User A pending request not found"
    
    print(f"Owner approving User A join request (Member ID: {usera_member_id})...")
    res = requests.put(f"{BASE_URL}/api/communities/{community_id}/requests/{usera_member_id}/approve", headers=owner_headers)
    assert_status(res, 200)
    time.sleep(0.5)

    # Confirm MEMBERSHIP_APPROVED notification
    notif = find_notification(usera_headers, "MEMBERSHIP_APPROVED")
    assert notif is not None, "MEMBERSHIP_APPROVED notification not received by User A"
    print(f"[SUCCESS] MEMBERSHIP_APPROVED verified: {notif['message']}")

    # 4. User B joins private community (Status = PENDING)
    print("User B requesting to join private community...")
    res = requests.post(f"{BASE_URL}/api/communities/{community_id}/join", headers=userb_headers)
    assert_status(res, 200)

    # 5. Owner rejects User B -> triggers MEMBERSHIP_REJECTED
    res = requests.get(f"{BASE_URL}/api/communities/{community_id}/requests", headers=owner_headers)
    assert_status(res, 200)
    pending_list = res.json()
    userb_member_id = None
    for req in pending_list:
        if req["username"] == userb_username:
            userb_member_id = req["memberId"]
            break
            
    assert userb_member_id is not None, "User B pending request not found"
    
    print(f"Owner rejecting User B join request (Member ID: {userb_member_id})...")
    res = requests.put(f"{BASE_URL}/api/communities/{community_id}/requests/{userb_member_id}/reject", headers=owner_headers)
    assert_status(res, 200)
    time.sleep(0.5)

    # Confirm MEMBERSHIP_REJECTED notification
    notif = find_notification(userb_headers, "MEMBERSHIP_REJECTED")
    assert notif is not None, "MEMBERSHIP_REJECTED notification not received by User B"
    print(f"[SUCCESS] MEMBERSHIP_REJECTED verified: {notif['message']}")

    # 6. Owner promotes User A to Moderator -> triggers MEMBER_PROMOTED
    print("Owner fetching community members list...")
    res = requests.get(f"{BASE_URL}/api/communities/{community_id}/members", headers=owner_headers)
    assert_status(res, 200)
    members_list = res.json()
    usera_approved_member_id = None
    for m in members_list:
        if m["username"] == usera_username:
            usera_approved_member_id = m["memberId"]
            break
            
    assert usera_approved_member_id is not None, "User A approved member ID not found"
    
    print(f"Owner promoting User A to MODERATOR (Member ID: {usera_approved_member_id})...")
    res = requests.put(f"{BASE_URL}/api/communities/{community_id}/members/{usera_approved_member_id}/role", headers=owner_headers, json={
        "role": "MODERATOR"
    })
    assert_status(res, 200)
    time.sleep(0.5)

    # Confirm MEMBER_PROMOTED notification
    notif = find_notification(usera_headers, "MEMBER_PROMOTED")
    assert notif is not None, "MEMBER_PROMOTED notification not received by User A"
    print(f"[SUCCESS] MEMBER_PROMOTED verified: {notif['message']}")

    # 7. Owner creates decision (draft) and options, then publishes -> triggers DECISION_PUBLISHED
    print("Owner creating decision draft...")
    res = requests.post(f"{BASE_URL}/api/decisions", headers=owner_headers, json={
        "title": f"Notif E2E Decision {timestamp}",
        "description": "Integration decision testing",
        "communityId": community_id,
        "isPublic": True,
        "votingType": "SINGLE_CHOICE",
        "votingEndTime": "2030-08-01T12:00:00",
        "deadline": "2030-08-02T12:00:00"
    })
    assert_status(res, 201)
    decision_id = res.json()["id"]
    
    print("Owner adding Option A...")
    res = requests.put(f"{BASE_URL}/api/decisions/{decision_id}/options", headers=owner_headers, json={"title": "Option A", "description": "Desc"})
    assert_status(res, 201)
    option_id_a = res.json()["id"]

    print("Owner adding Option B...")
    res = requests.put(f"{BASE_URL}/api/decisions/{decision_id}/options", headers=owner_headers, json={"title": "Option B", "description": "Desc"})
    assert_status(res, 201)

    print("Owner publishing decision...")
    res = requests.put(f"{BASE_URL}/api/decisions/{decision_id}/publish", headers=owner_headers)
    assert_status(res, 200)
    time.sleep(0.5)

    # Confirm DECISION_PUBLISHED notification
    notif = find_notification(usera_headers, "DECISION_PUBLISHED")
    assert notif is not None, "DECISION_PUBLISHED notification not received by User A"
    print(f"[SUCCESS] DECISION_PUBLISHED verified: {notif['message']}")

    # 8. User A creates a comment -> triggers COMMENT_CREATED
    print("User A creating comment...")
    res = requests.put(f"{BASE_URL}/api/decisions/{decision_id}/comments", headers=usera_headers, json={
        "content": "Verify CommentCreatedEvent notification triggers"
    })
    assert_status(res, 201)
    comment_id = res.json()["id"]

    time.sleep(0.5)
    # Confirm COMMENT_CREATED notification
    notif = find_notification(owner_headers, "COMMENT_CREATED")
    assert notif is not None, "COMMENT_CREATED notification not received by Owner"
    print(f"[SUCCESS] COMMENT_CREATED verified: {notif['message']}")

    # 9. Owner replies to User A comment -> triggers REPLY_CREATED
    print("Owner replying to User A comment...")
    res = requests.put(f"{BASE_URL}/api/decisions/{decision_id}/comments/{comment_id}/replies", headers=owner_headers, json={
        "content": "Verify ReplyCreatedEvent notification triggers"
    })
    assert_status(res, 201)
    time.sleep(0.5)

    # Confirm REPLY_CREATED notification
    notif = find_notification(usera_headers, "REPLY_CREATED")
    assert notif is not None, "REPLY_CREATED notification not received by User A"
    print(f"[SUCCESS] REPLY_CREATED verified: {notif['message']}")

    # 10. User A votes on decision -> triggers VOTE_SUBMITTED
    print("User A submitting vote...")
    res = requests.put(f"{BASE_URL}/api/decisions/{decision_id}/votes", headers=usera_headers, json={
        "optionIds": [option_id_a]
    })
    assert_status(res, 200)
    time.sleep(0.5)

    # Confirm VOTE_SUBMITTED notification
    notif = find_notification(owner_headers, "VOTE_SUBMITTED")
    assert notif is not None, "VOTE_SUBMITTED notification not received by Owner"
    print(f"[SUCCESS] VOTE_SUBMITTED verified: {notif['message']}")

    # 11. Owner closes poll early -> triggers POLL_CLOSED
    print("Owner closing poll early...")
    res = requests.put(f"{BASE_URL}/api/decisions/{decision_id}/poll/close", headers=owner_headers)
    assert_status(res, 200)
    time.sleep(0.5)

    # Confirm POLL_CLOSED notification
    notif = find_notification(owner_headers, "POLL_CLOSED")
    assert notif is not None, "POLL_CLOSED notification not received by Owner"
    print(f"[SUCCESS] POLL_CLOSED verified: {notif['message']}")

    # 12. User A (Moderator) pins the decision -> triggers DECISION_PINNED
    print("User A (Moderator) pinning decision...")
    res = requests.put(f"{BASE_URL}/api/moderation/decisions/{decision_id}/pin", headers=usera_headers)
    assert_status(res, 200)
    time.sleep(0.5)

    # Confirm DECISION_PINNED notification
    notif = find_notification(owner_headers, "DECISION_PINNED")
    assert notif is not None, "DECISION_PINNED notification not received by Owner"
    print(f"[SUCCESS] DECISION_PINNED verified: {notif['message']}")

    # 13. User A (Moderator) unpins the decision -> triggers DECISION_UNPINNED
    print("User A (Moderator) unpinning decision...")
    res = requests.put(f"{BASE_URL}/api/moderation/decisions/{decision_id}/unpin", headers=usera_headers)
    assert_status(res, 200)
    time.sleep(0.5)

    # Confirm DECISION_UNPINNED notification
    notif = find_notification(owner_headers, "DECISION_UNPINNED")
    assert notif is not None, "DECISION_UNPINNED notification not received by Owner"
    print(f"[SUCCESS] DECISION_UNPINNED verified: {notif['message']}")

    # 14. User A (Moderator) locks the decision -> triggers DECISION_LOCKED
    print("User A (Moderator) locking decision...")
    res = requests.put(f"{BASE_URL}/api/moderation/decisions/{decision_id}/lock", headers=usera_headers)
    assert_status(res, 200)
    time.sleep(0.5)

    # Confirm DECISION_LOCKED notification
    notif = find_notification(owner_headers, "DECISION_LOCKED")
    assert notif is not None, "DECISION_LOCKED notification not received by Owner"
    print(f"[SUCCESS] DECISION_LOCKED verified: {notif['message']}")

    # 15. User A (Moderator) unlocks the decision -> triggers DECISION_UNLOCKED
    print("User A (Moderator) unlocking decision...")
    res = requests.put(f"{BASE_URL}/api/moderation/decisions/{decision_id}/unlock", headers=usera_headers)
    assert_status(res, 200)
    time.sleep(0.5)

    # Confirm DECISION_UNLOCKED notification
    notif = find_notification(owner_headers, "DECISION_UNLOCKED")
    assert notif is not None, "DECISION_UNLOCKED notification not received by Owner"
    print(f"[SUCCESS] DECISION_UNLOCKED verified: {notif['message']}")

    # 16. Owner creates a comment, and User A (Moderator) deletes it -> triggers COMMENT_REMOVED
    print("Owner creating comment to be removed...")
    res = requests.put(f"{BASE_URL}/api/decisions/{decision_id}/comments", headers=owner_headers, json={
        "content": "Inappropriate comment text to be removed"
    })
    assert_status(res, 201)
    owner_comment_id = res.json()["id"]

    print("User A (Moderator) deleting Owner comment...")
    res = requests.delete(f"{BASE_URL}/api/moderation/comments/{owner_comment_id}", headers=usera_headers)
    assert_status(res, 200)
    time.sleep(0.5)

    # Confirm COMMENT_REMOVED notification
    notif = find_notification(owner_headers, "COMMENT_REMOVED")
    assert notif is not None, "COMMENT_REMOVED notification not received by Owner"
    print(f"[SUCCESS] COMMENT_REMOVED verified: {notif['message']}")

    print_section("SECURITY & ISOLATION VERIFICATION")

    # Fetch one of User A's notification IDs
    res = requests.get(f"{BASE_URL}/api/notifications", headers=usera_headers)
    assert_status(res, 200)
    usera_notif_id = res.json()["content"][0]["id"]

    # Owner attempts to mark User A's notification as read -> Expected 403 Forbidden
    print(f"Owner attempting to read User A notification (ID: {usera_notif_id})...")
    res = requests.put(f"{BASE_URL}/api/notifications/{usera_notif_id}/read", headers=owner_headers)
    assert_status(res, 403)
    print("[SUCCESS] Cross-user update blocked with 403 Forbidden.")

    # Owner attempts to delete User A's notification -> Expected 403 Forbidden
    print(f"Owner attempting to delete User A notification (ID: {usera_notif_id})...")
    res = requests.delete(f"{BASE_URL}/api/notifications/{usera_notif_id}", headers=owner_headers)
    assert_status(res, 403)
    print("[SUCCESS] Cross-user deletion blocked with 403 Forbidden.")

    print_section("TEARDOWN & CLEANUP")
    
    res = requests.delete(f"{BASE_URL}/api/decisions/{decision_id}", headers=owner_headers)
    print(f"Delete Decision status: {res.status_code}")
    res = requests.delete(f"{BASE_URL}/api/communities/{community_id}", headers=owner_headers)
    print(f"Delete Community status: {res.status_code}")

    print("\n[SUCCESS] E2E Integration Verification completed cleanly! All 13 event chains and user isolation verified successfully.")

if __name__ == "__main__":
    main()

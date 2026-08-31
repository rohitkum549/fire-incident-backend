import urllib.request
import json
import time
import sys

# Base server URL
BASE_URL = "http://localhost:8080"

def make_request(path, method="GET", data=None, token=None):
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
        
    req_data = json.dumps(data).encode("utf-8") if data else None
    req = urllib.request.Request(url, data=req_data, headers=headers, method=method)
    
    try:
        with urllib.request.urlopen(req) as response:
            res_content = response.read().decode("utf-8")
            return json.loads(res_content) if res_content else {}
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode("utf-8")
        print(f"[-] HTTP Error {e.code} on {method} {path}: {err_msg}")
        raise e
    except Exception as e:
        print(f"[-] Connection failed on {method} {path}: {str(e)}")
        raise e

def run_e2e_tests():
    print("[*] Starting E2E API Verification...")

    # 1. Health check
    try:
        health = make_request("/api/v1/health")
        print(f"[+] Health check passed: {health.get('status')}")
    except Exception:
        print("[-] Health check failed. Backend server might not be running.")
        sys.exit(1)

    # 2. Setup Geography
    print("[*] Setting up geography zones...")
    country = make_request("/api/v1/geography/countries", "POST", {
        "name": "United States",
        "iso_code": f"USA-{int(time.time())}" # unique ISO code
    })
    country_id = country["id"]
    print(f"[+] Created Country: ID={country_id}")

    state = make_request("/api/v1/geography/states", "POST", {
        "country_id": country_id,
        "name": "California",
        "code": "CA"
    })
    state_id = state["id"]
    print(f"[+] Created State: ID={state_id}")

    city = make_request("/api/v1/geography/cities", "POST", {
        "state_id": state_id,
        "name": "San Francisco"
    })
    city_id = city["id"]
    print(f"[+] Created City: ID={city_id}")

    station = make_request("/api/v1/stations", "POST", {
        "cityId": city_id,
        "name": "SFFD Station 1",
        "address": "251 Lafayette St, San Francisco",
        "latitude": 37.774900,
        "longitude": -122.419400
    })
    station_id = station["id"]
    print(f"[+] Created Fire Station: ID={station_id}")

    # 3. Onboard Admin & Login
    print("[*] Onboarding Admin staff...")
    admin_uname = f"admin_alice_{int(time.time())}"
    admin_reg = make_request("/api/v1/auth/register", "POST", {
        "username": admin_uname,
        "email": f"alice_{int(time.time())}@example.com",
        "password": "Password123!",
        "first_name": "Alice",
        "last_name": "Smith",
        "phone_number": "+15550100",
        "role_names": ["ROLE_ADMIN"],
        "station_id": station_id,
        "employee_code": f"EMP-ADM-{int(time.time())}"
    })
    admin_id = admin_reg["id"]
    print(f"[+] Registered Admin: ID={admin_id}")

    admin_login = make_request("/api/v1/auth/login", "POST", {
        "username": admin_uname,
        "password": "Password123!"
    })
    admin_token = admin_login["access_token"]
    print(f"[+] Logged in Admin. JWT token retrieved.")

    # 4. Onboard Firefighter & Login
    print("[*] Onboarding Firefighter staff...")
    ff_uname = f"ff_bob_{int(time.time())}"
    ff_reg = make_request("/api/v1/auth/register", "POST", {
        "username": ff_uname,
        "email": f"bob_{int(time.time())}@example.com",
        "password": "Password123!",
        "first_name": "Bob",
        "last_name": "Builder",
        "phone_number": "+15550200",
        "role_names": ["ROLE_FIREFIGHTER"],
        "station_id": station_id,
        "employee_code": f"EMP-FF-{int(time.time())}"
    })
    ff_id = ff_reg["id"]
    print(f"[+] Registered Firefighter: ID={ff_id}")

    ff_login = make_request("/api/v1/auth/login", "POST", {
        "username": ff_uname,
        "password": "Password123!"
    })
    ff_token = ff_login["access_token"]
    print(f"[+] Logged in Firefighter. JWT token retrieved.")

    # 5. Onboard Citizen
    print("[*] Onboarding Citizen user...")
    citizen_uname = f"citizen_jane_{int(time.time())}"
    citizen_reg = make_request("/api/v1/auth/register", "POST", {
        "username": citizen_uname,
        "email": f"jane_{int(time.time())}@example.com",
        "password": "Password123!",
        "first_name": "Jane",
        "last_name": "Doe",
        "phone_number": "+15550300",
        "role_names": ["ROLE_CITIZEN"]
    })
    citizen_id = citizen_reg["id"]
    print(f"[+] Registered Citizen: ID={citizen_id}")

    # 6. Firefighter Shift check-in
    print("[*] Performing Firefighter shift check-in...")
    shift = make_request("/api/v1/shifts/check-in", "POST", {
        "employee_id": ff_id,
        "station_id": station_id,
        "check_in_latitude": 37.774900,
        "check_in_longitude": -122.419400
    }, ff_token)
    shift_id = shift["id"]
    print(f"[+] Shift checked in successfully. ID={shift_id}, Status={shift['status']}")

    # 7. Get Seeded Incident Category
    categories = make_request("/api/v1/complaints/categories")
    category_id = categories[0]["id"]
    category_name = categories[0]["name"]
    print(f"[+] Using seeded incident category: {category_name} (ID={category_id})")

    # 8. Submit Complaint
    print("[*] Submitting Citizen Complaint...")
    complaint = make_request("/api/v1/complaints", "POST", {
        "reporterId": citizen_id,
        "categoryId": category_id,
        "latitude": 37.775000,
        "longitude": -122.419000,
        "severity": "HIGH",
        "description": "Smoke visible from basement windows."
    })
    complaint_id = complaint["id"]
    print(f"[+] Citizen complaint registered successfully. ID={complaint_id}, Status={complaint['status']}")

    # 9. Admin escalates complaint to incident
    print("[*] Admin escalating complaint to incident response...")
    incident = make_request("/api/v1/incidents", "POST", {
        "complaintId": complaint_id,
        "stationId": station_id,
        "categoryId": category_id,
        "severity": "HIGH",
        "latitude": 37.775000,
        "longitude": -122.419000,
        "notes": "Escalated by dispatch officer. Dispatching Engine 1."
    }, admin_token)
    incident_id = incident["id"]
    print(f"[+] Incident dispatched. ID={incident_id}, Status={incident['status']}")

    # 10. Firefighter transitions incident to IN_PROGRESS
    print("[*] Firefighter transitioning incident to IN_PROGRESS...")
    inc_progress = make_request(f"/api/v1/incidents/{incident_id}/status", "PATCH", {
        "status": "IN_PROGRESS",
        "notes": "Engine 1 arrived on scene. Deploying water lines."
    }, ff_token)
    print(f"[+] Incident transitioned. Status={inc_progress['status']}")

    # 11. Firefighter resolves incident
    print("[*] Firefighter resolving incident...")
    inc_resolved = make_request(f"/api/v1/incidents/{incident_id}/status", "PATCH", {
        "status": "RESOLVED",
        "notes": "Fire fully extinguished. Structure ventilated. Scened cleared."
    }, ff_token)
    print(f"[+] Incident resolved. Status={inc_resolved['status']}, ResolvedAt={inc_resolved['resolvedAt']}")

    # 12. Firefighter shift check-out (within 500m geofence)
    print("[*] Performing Firefighter shift check-out (within 500m)...")
    shift_checkout = make_request("/api/v1/shifts/check-out", "PATCH", {
        "shift_id": shift_id,
        "check_out_latitude": 37.775200, # ~30m displacement from station
        "check_out_longitude": -122.419400
    }, ff_token)
    print(f"[+] Shift checked out successfully. Status={shift_checkout['status']}")

    print("\n[+] SUCCESS: E2E API Verification Complete! All flows passed correctly.")

if __name__ == "__main__":
    run_e2e_tests()

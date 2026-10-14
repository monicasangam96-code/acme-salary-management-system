#!/usr/bin/env python3
"""
ACME Salary Management System — Synthetic Employee & Salary Data Generator
Generates 10,000 employees with salary records distributed across countries,
departments, and job levels.

Usage:
    python generate.py --output /path/to/output.csv
"""

import argparse
import csv
import random
from datetime import date, timedelta
import json

# ---------------------------------------------------------------------------
# Distribution Profiles
# ---------------------------------------------------------------------------
COUNTRIES = [
    # (country_code, country_name, currency_code, salary_min, salary_max)
    ("US",  "United States (US)",   "USD", 60_000,  220_000),
    ("UK",  "United Kingdom (UK)",  "GBP", 30_000,  140_000),
    ("DE",  "Germany (DE)",         "EUR", 35_000,  130_000),
    ("JP",  "Japan (JP)",           "JPY", 3_000_000, 15_000_000),
    ("FR",  "France (FR)",          "EUR", 30_000,  120_000),
    ("IN",  "India (IN)",           "INR", 300_000, 5_000_000),
    ("CA",  "Canada (CA)",          "CAD", 45_000,  160_000),
    ("AU",  "Australia (AU)",       "AUD", 50_000,  170_000),
    ("SG",  "Singapore (SG)",       "SGD", 35_000,  140_000),
    ("KR",  "South Korea (KR)",     "KRW", 20_000_000, 90_000_000),
    ("BR",  "Brazil (BR)",          "BRL", 30_000,  400_000),
    ("MX",  "Mexico (MX)",          "MXN", 80_000,  900_000),
    ("PL",  "Poland (PL)",          "PLN", 35_000,  250_000),
    ("SE",  "Sweden (SE)",          "SEK", 350_000, 1_200_000),
    ("TH",  "Thailand (TH)",        "THB", 200_000, 2_000_000),
]

COUNTRY_WEIGHTS = [35, 12, 10, 8, 7, 10, 4, 3, 3, 2, 1, 1, 1, 1, 2]  # sum ≈ 100

DEPARTMENTS = [
    ("Engineering", 40),
    ("Sales", 15),
    ("Marketing", 10),
    ("HR", 5),
    ("Finance", 10),
    ("Operations", 10),
    ("Legal", 5),
    ("Other", 5),
]

JOB_LEVELS = {
    "Engineering": [
        "Junior Software Engineer",
        "Software Engineer",
        "Senior Software Engineer",
        "Lead Software Engineer",
        "Director of Engineering",
    ],
    "Sales": [
        "Sales Associate",
        "Account Executive",
        "Senior Account Executive",
        "Sales Manager",
        "Sales Director",
    ],
    "Marketing": [
        "Marketing Coordinator",
        "Marketing Analyst",
        "Marketing Manager",
        "Content Manager",
        "VP of Marketing",
    ],
    "HR": [
        "HR Coordinator",
        "HR Specialist",
        "HR Manager",
        "Senior HR Manager",
        "VP of HR",
    ],
    "Finance": [
        "Financial Analyst",
        "Senior Financial Analyst",
        "Accountant",
        "Finance Manager",
        "CFO",
    ],
    "Operations": [
        "Operations Associate",
        "Operations Analyst",
        "Operations Manager",
        "Senior Operations Manager",
        "VP of Operations",
    ],
    "Legal": [
        "Legal Assistant",
        "Paralegal",
        "Legal Counsel",
        "Senior Legal Counsel",
        "General Counsel",
    ],
    "Other": [
        "Support Specialist",
        "Coordinator",
        "Manager",
        "Senior Manager",
        "Director",
    ],
}

FIRST_NAMES = [
    "John", "Jane", "Alice", "Bob", "Carol", "David", "Eva", "Frank", "Grace",
    "Henry", "Iris", "Jack", "Karen", "Leo", "Mia", "Nathan", "Olivia", "Paul",
    "Quinn", "Rachel", "Sam", "Tina", "Uma", "Victor", "Wendy", "Xavier", "Yuki",
    "Zara", "Carlos", "Priya", "Wei", "Hiroshi", "Marie", "Hans", "Sarah", "Tom",
    "Emily", "Ryan", "Emma", "James", "Olivia", "Harry", "Charlotte", "Noah", "Ava",
    "Liam", "Sophia", "Mason", "Isabella", "Ethan", "Mia", "Logan", "Amelia", "Lucas",
]

LAST_NAMES = [
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
    "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
    "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
    "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker",
    "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
    "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell",
    "Carter", "Roberts", "Muller", "Schmidt", "Weber", "Fischer", "Tanaka", "Suzuki",
    "Watanabe", "Park", "Kim", "Choi", "Lee", "Patel", "Sharma", "Kumar", "Singh",
]

CITIES_BY_COUNTRY = {
    "US": [("Mountain View", "CA"), ("New York", "NY"), ("Austin", "TX"), ("Seattle", "WA"), ("Chicago", "IL"), ("Denver", "CO")],
    "UK": [("London", None), ("Manchester", None), ("Edinburgh", None)],
    "DE": [("Berlin", None), ("Munich", "Bavaria"), ("Frankfurt", "Hesse")],
    "JP": [("Tokyo", None), ("Osaka", None), ("Yokohama", None)],
    "FR": [("Paris", None), ("Lyon", None)],
    "IN": [("Bangalore", "Karnataka"), ("Mumbai", "Maharashtra"), ("New Delhi", None)],
}


def pick_country(rng):
    return random.choices(COUNTRIES, weights=COUNTRY_WEIGHTS, k=1)[0]


def pick_department(rng):
    depts, wts = zip(*DEPARTMENTS)
    dept = random.choices(depts, weights=wts, k=1)[0]
    levels = JOB_LEVELS[dept]
    level = random.choices(levels, weights=[5, 25, 35, 25, 10], k=1)[0]
    return dept, level


def generate_email(first, last):
    base = f"{first.lower()}.{last.lower()}{random.randint(1,99)}"
    domains = ["@acme.com", "@acme.co"]
    return base + random.choice(domains)


def generate_phone(country_code):
    prefixes = {"US": "+1-555-", "UK": "+44-7911-", "DE": "+49-30-", "JP": "+81-3-",
                "FR": "+33-1-", "IN": "+91-98-", "CA": "+1-416-", "AU": "+61-2-",
                "SG": "+65-6-", "KR": "+82-2-", "BR": "+55-11-", "MX": "+52-55-",
                "PL": "+48-22-", "SE": "+46-8-", "TH": "+66-2-"}
    prefix = prefixes.get(country_code, "+0-0-")
    return f"{prefix}{random.randint(100,999):03d}{random.randint(1000,9999)}"


def main():
    parser = argparse.ArgumentParser(description="Generate synthetic employee data")
    parser.add_argument("--output", default="/tmp/acme_employees.csv", help="Output CSV path")
    parser.add_argument("--count", type=int, default=10000, help="Number of employees to generate")
    parser.add_argument("--seed", type=int, default=42, help="Random seed for reproducibility")
    args = parser.parse_args()

    rng = random.Random(args.seed)
    rows = []

    for i in range(1, args.count + 1):
        emp_no = f"EMP-{i:05d}"
        first_name = rng.choice(FIRST_NAMES)
        last_name = rng.choice(LAST_NAMES)
        country, salary_min, salary_max = pick_country(rng)
        dept, job_level = pick_department(rng)
        city, state = rng.choice(CITIES_BY_COUNTRY.get(country, [("Unknown", None)]))

        days_employed = rng.randint(180, 7300)
        start_date = date.today() - timedelta(days=days_employed)

        if rng.random() < 0.02:  # ~2% terminated
            status = "TERMINATED"
            end_date = date.today() - timedelta(days=rng.randint(1, 30))
            salary_min *= 0.8  # Terminated employees tend to have lower salaries
        else:
            status = "ACTIVE"
            end_date = None

        salary_amount = rng.randint(salary_min, salary_max)
        effective_date = start_date + timedelta(days=rng.randint(0, 365))
        previous_salary = int(salary_amount * rng.uniform(0.9, 1.0)) if rng.random() < 0.3 else None

        hours_worked = round(rng.uniform(140, 208), 2)
        pay_date = date.today().replace(day=1) - timedelta(days=rng.randint(0, 30))

        state_tax = round(salary_amount * rng.uniform(0.03, 0.12), 2)
        federal_tax = round(salary_amount * rng.uniform(0.05, 0.25), 2)
        deductions = json.dumps({
            "state_tax": state_tax,
            "federal_tax": federal_tax,
            "social_security": round(salary_amount * 0.062, 2),
            "medicare": round(salary_amount * 0.0145, 2),
            "disability_insurance": round(salary_amount * rng.uniform(0.0, 0.01), 2),
            "health_insurance": round(salary_amount * rng.uniform(0.03, 0.08), 2),
        })

        rows.append({
            "emp_no": emp_no,
            "first_name": first_name,
            "last_name": last_name,
            "email": generate_email(first_name, last_name),
            "phone": generate_phone(country),
            "address_line1": f"{rng.randint(1,9999)} Main Street",
            "city": city,
            "state_province": state or "",
            "zip_code": f"{rng.randint(10000, 99999)}",
            "country": country,
            "dept": dept,
            "job_title": job_level,
            "employment_status": status,
            "start_date": start_date.isoformat(),
            "end_date": end_date.isoformat() if end_date else "",
            "is_active": 1 if status == "ACTIVE" else 0,
            "salary_amount": salary_amount,
            "currency_code": [c for c in COUNTRIES if c[0] == country][0][2],
            "effective_date": effective_date.isoformat(),
            "previous_salary": previous_salary or "",
            "hours_worked": hours_worked,
            "pay_date": pay_date.isoformat(),
            "gross_pay": salary_amount / 12.0 if rng.random() < 0.7 else "",
            "net_pay": salary_amount / 13.5 if rng.random() < 0.7 else "",
            "deductions": deductions,
        })

    # Write CSV
    keys = list(rows[0].keys())
    with open(args.output, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=keys)
        writer.writeheader()
        writer.writerows(rows)

    print(f"Generated {len(rows)} employees → {args.output}")


if __name__ == "__main__":
    main()

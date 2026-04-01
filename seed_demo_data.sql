-- =============================================================
--  DEMO SEED DATA  -  Event Management System
--  Import this AFTER importing setup_database.sql
--  All accounts use password: password123
-- =============================================================

USE event_management_db;

-- ─── USERS ────────────────────────────────────────────────────
-- SHA-256 hash of "password123"
-- echo -n "password123" | sha256sum
-- = ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f

INSERT INTO users (email, password_hash, role) VALUES
  ('techfest@org.com',   'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ORGANIZATION'),
  ('musicworld@org.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ORGANIZATION'),
  ('alice@user.com',     'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'PARTICIPANT'),
  ('bob@user.com',       'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'PARTICIPANT'),
  ('charlie@vol.com',    'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'VOLUNTEER'),
  ('diana@vol.com',      'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'VOLUNTEER');

-- ─── ROLE PROFILES ────────────────────────────────────────────

INSERT INTO organizations (user_id, organization_name) VALUES
  (1, 'TechFest Society'),
  (2, 'MusicWorld Events');

INSERT INTO participants (user_id, first_name, last_name) VALUES
  (3, 'Alice', 'Sharma'),
  (4, 'Bob',   'Verma');

INSERT INTO volunteers (user_id, first_name, last_name, hours_logged) VALUES
  (5, 'Charlie', 'Khan', 0),
  (6, 'Diana',   'Patel', 0);

-- ─── EVENTS (by TechFest Society - org_id = 1) ────────────────

INSERT INTO events (org_id, title, description, event_date, location, event_type, max_capacity, status) VALUES
  (1, 'AI & ML Summit 2025',
   'A full-day deep dive into Artificial Intelligence, Machine Learning, and cutting-edge research. Featuring keynotes, workshops, and networking sessions.',
   '2026-04-15 10:00:00', 'Auditorium A, Tech Campus', 'CONFERENCE', 150, 'PUBLISHED'),

  (1, 'Hackathon: Code for Good',
   '24-hour hackathon where teams build solutions for social impact. Food, prizes, and mentors provided. All skill levels welcome!',
   '2026-04-20 09:00:00', 'Innovation Lab, Block C', 'CONCERT', 80, 'PUBLISHED'),

  (1, 'Spring Tech Expo',
   'Annual showcase of student and startup projects. Come explore demos, live pitches, and sponsor booths.',
   '2026-05-01 11:00:00', 'Main Grounds, Tech Campus', 'CONFERENCE', 300, 'DRAFT');

-- ─── EVENTS (by MusicWorld Events -  org_id = 2) ──────────────

INSERT INTO events (org_id, title, description, event_date, location, event_type, max_capacity, status) VALUES
  (2, 'Beats & Brews Festival',
   'An outdoor music festival featuring 10+ live bands across two stages. Street food, drinks, and good vibes all evening long.',
   '2026-04-18 17:00:00', 'City Park, Open Ground', 'CONCERT', 500, 'PUBLISHED'),

  (2, 'Classical Night Gala',
   'An elegant evening of classical music performed by the City Symphony Orchestra. Black-tie optional. Dinner included.',
   '2026-04-25 19:30:00', 'Grand Hall, Convention Centre', 'CONCERT', 200, 'PUBLISHED'),

  (2, 'Royal Garden Wedding Showcase',
   'An exclusive showcase for couples planning their wedding. Featuring live demonstrations, vendor meet-and-greet, and cake tasting.',
   '2026-05-10 12:00:00', 'The Royal Gardens, Hillside', 'WEDDING', 100, 'PUBLISHED');

-- ─── REGISTRATIONS ────────────────────────────────────────────
-- Alice (participant, user_id=3) RSVPs for AI Summit and Beats Fest
INSERT INTO registrations (event_id, user_id, registration_role, application_text, status, ticket_number, has_entered) VALUES
  (1, 3, 'PARTICIPANT', NULL, 'ACCEPTED', 'TKT-ALICE001', TRUE),
  (4, 3, 'PARTICIPANT', NULL, 'ACCEPTED', 'TKT-ALICE002', FALSE);

-- Bob (participant, user_id=4) RSVPs for Classical Night
INSERT INTO registrations (event_id, user_id, registration_role, application_text, status, ticket_number, has_entered) VALUES
  (5, 4, 'PARTICIPANT', NULL, 'ACCEPTED', 'TKT-BOB0001', FALSE);

-- Charlie (volunteer, user_id=5) applies to volunteer for AI Summit (ACCEPTED) & Hackathon (PENDING)
INSERT INTO registrations (event_id, user_id, registration_role, application_text, status, ticket_number, has_entered) VALUES
  (1, 5, 'VOLUNTEER', 'I have experience organizing college tech events and can assist with speaker coordination and crowd management.', 'ACCEPTED', 'TKT-CHARLIE1', TRUE),
  (2, 5, 'VOLUNTEER', 'I am a competitive programmer and would love to help run the hackathon and assist participating teams.', 'PENDING', NULL, FALSE);

-- Diana (volunteer, user_id=6) applies to volunteer for Beats Fest (PENDING) & Classical Night (BLOCKED)
INSERT INTO registrations (event_id, user_id, registration_role, application_text, status, ticket_number, has_entered) VALUES
  (4, 6, 'VOLUNTEER', 'I am a music lover with backstage coordination experience from college fests. Happy to help with stage and crowd.', 'PENDING', NULL, FALSE),
  (5, 6, 'VOLUNTEER', 'I have formal experience assisting at classical concerts and can help with seating coordination and ushering.', 'BLOCKED', NULL, FALSE);

-- =============================================================
--  SUMMARY OF DEMO ACCOUNTS (all password: password123)
-- =============================================================
--  ORGANIZATION:
--    techfest@org.com     → TechFest Society (3 events: 2 published, 1 draft)
--    musicworld@org.com   → MusicWorld Events (3 published events)
--
--  PARTICIPANT:
--    alice@user.com       → RSVP'd to AI Summit & Beats Fest (both with tickets)
--    bob@user.com         → RSVP'd to Classical Night (with ticket)
--
--  VOLUNTEER:
--    charlie@vol.com      → Accepted for AI Summit, Pending for Hackathon
--    diana@vol.com        → Pending for Beats Fest, Blocked for Classical Night
-- =============================================================

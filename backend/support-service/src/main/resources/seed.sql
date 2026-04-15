-- Seed data for the support-service (borrows, fines, notifications).
-- Core-service users: 1=student@studentmail.ul.ie, 2=staff@ul.ie, 3=admin@ul.ie,
--                     100=alice.walsh, 101=bob.ryan, 102=carol.murphy
-- Core-service books start at id 5.
-- High IDs (1000+) avoid collisions with any auto-generated rows from prior runs.

INSERT INTO borrows (id, user_id, book_id, status, borrowed_at, deadline, returned_at) VALUES
    -- student (id 1): two active, one returned, one overdue
    (1001, 1, 5,  'BORROWED', NOW() - INTERVAL '3 days',  NOW() + INTERVAL '11 days', NULL),
    (1002, 1, 6,  'BORROWED', NOW() - INTERVAL '7 days',  NOW() + INTERVAL '7 days',  NULL),
    (1003, 1, 7,  'RETURNED', NOW() - INTERVAL '30 days', NOW() - INTERVAL '16 days', NOW() - INTERVAL '18 days'),
    (1004, 1, 8,  'OVERDUE',  NOW() - INTERVAL '20 days', NOW() - INTERVAL '6 days',  NULL),
    -- alice.walsh (id 100): one active, one returned
    (1005, 100, 9,  'BORROWED', NOW() - INTERVAL '5 days',  NOW() + INTERVAL '9 days',  NULL),
    (1006, 100, 10, 'RETURNED', NOW() - INTERVAL '45 days', NOW() - INTERVAL '31 days', NOW() - INTERVAL '33 days'),
    -- bob.ryan (id 101): one overdue
    (1007, 101, 11, 'OVERDUE',  NOW() - INTERVAL '25 days', NOW() - INTERVAL '11 days', NULL),
    -- carol.murphy (id 102): one active
    (1008, 102, 12, 'BORROWED', NOW() - INTERVAL '1 day',   NOW() + INTERVAL '13 days', NULL),
    -- staff (id 2): one active long-loan
    (1009, 2, 13, 'BORROWED', NOW() - INTERVAL '2 days',  NOW() + INTERVAL '26 days', NULL)
ON CONFLICT DO NOTHING;

INSERT INTO fines (id, borrow_id, user_id, amount, issued_at, paid, acknowledged, paid_at) VALUES
    -- fine for student (id 1) overdue borrow (borrow 1004) — unpaid
    (1001, 1004, 1, 3.00, NOW() - INTERVAL '5 days', FALSE, FALSE, NULL),
    -- fine for bob.ryan (id 101) overdue borrow (borrow 1007) — paid and acknowledged
    (1002, 1007, 101, 5.50, NOW() - INTERVAL '10 days', TRUE, TRUE, NOW() - INTERVAL '3 days')
ON CONFLICT DO NOTHING;

INSERT INTO notifications (id, user_id, type, message, read, created_at) VALUES
    -- student (id 1)
    (1001, 1, 'BORROW_CREATED',  'You have borrowed "Clean Architecture Foundations". Due in 14 days.',           TRUE,  NOW() - INTERVAL '3 days'),
    (1002, 1, 'BORROW_CREATED',  'You have borrowed "SOLID Design Workshop". Due in 14 days.',                     TRUE,  NOW() - INTERVAL '7 days'),
    (1003, 1, 'BORROW_DUE_SOON', '"SOLID Design Workshop" is due in 7 days. Please return it on time.',            FALSE, NOW()),
    (1004, 1, 'FINE_CREATED',    'A fine of €3.00 has been issued for the overdue book "Code Quality Handbook".', FALSE, NOW() - INTERVAL '5 days'),
    -- alice.walsh (id 100)
    (1005, 100, 'BORROW_CREATED',  'You have borrowed "Legacy Rescue Patterns". Due in 14 days.',                    TRUE,  NOW() - INTERVAL '5 days'),
    (1006, 100, 'BORROW_DUE_SOON', '"Legacy Rescue Patterns" is due in 9 days.',                                     FALSE, NOW()),
    -- bob.ryan (id 101)
    (1007, 101, 'FINE_CREATED',    'A fine of €5.50 has been issued for the overdue book "Object-Oriented Design Clinic".', TRUE, NOW() - INTERVAL '10 days'),
    -- carol.murphy (id 102)
    (1008, 102, 'BORROW_CREATED',  'You have borrowed "Service Boundaries in Practice". Due in 14 days.',            FALSE, NOW() - INTERVAL '1 day'),
    -- staff (id 2)
    (1009, 2, 'BORROW_CREATED',  'You have borrowed "Maintainable Systems Playbook". Due in 28 days.',             FALSE, NOW() - INTERVAL '2 days')
ON CONFLICT DO NOTHING;

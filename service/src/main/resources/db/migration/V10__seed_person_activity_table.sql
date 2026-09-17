-- Only link existing seed people and activities; do not recreate deleted data.
INSERT INTO person_activity (person_id, activity_id)
SELECT person.id, activity.id
FROM (VALUES
    ('modige.fjell@reiseapp.test', 1),
    ('rolige.maane@reiseapp.test', 1),
    ('kloke.stjerne@reiseapp.test', 2),
    ('raske.elv@reiseapp.test', 2),
    ('varme.sol@reiseapp.test', 3),
    ('nysgjerrige.glade.skog@reiseapp.test', 3),
    ('stille.hav.vind@reiseapp.test', 4),
    ('frie.milde.fugl.eng@reiseapp.test', 4)
) AS assignment(email, activity_id)
JOIN person ON lower(person.email) = assignment.email
JOIN activity ON activity.id = assignment.activity_id;

-- Match original people by email so deleted people are not recreated.
INSERT INTO person_room (person_id, room_number)
SELECT person.id, assignment.room_number
FROM (VALUES
    ('modige.fjell@reiseapp.test', 101),
    ('rolige.maane@reiseapp.test', 102),
    ('kloke.stjerne@reiseapp.test', 104),
    ('raske.elv@reiseapp.test', 104),
    ('varme.sol@reiseapp.test', 108),
    ('nysgjerrige.glade.skog@reiseapp.test', 108),
    ('stille.hav.vind@reiseapp.test', 108),
    ('frie.milde.fugl.eng@reiseapp.test', 108),
    ('lysende.morgen@reiseapp.test', 204),
    ('taalmodige.bjoern@reiseapp.test', 204),
    ('glade.rev@reiseapp.test', 103),
    ('trygge.gran@reiseapp.test', 105),
    ('milde.bekk@reiseapp.test', 105),
    ('modige.oern@reiseapp.test', 109),
    ('rolige.lyng@reiseapp.test', 109),
    ('kloke.ugle@reiseapp.test', 109),
    ('friske.bris@reiseapp.test', 109),
    ('varme.eng@reiseapp.test', 201),
    ('stoedige.stein@reiseapp.test', 205),
    ('nysgjerrige.ekorn@reiseapp.test', 205)
) AS assignment(email, room_number)
JOIN person ON lower(person.email) = assignment.email
JOIN room ON room.room_number = assignment.room_number;

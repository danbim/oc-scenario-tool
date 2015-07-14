# --- !Ups

INSERT INTO user (
  email,
  name,
  first_name,
  last_name,
  gender,
  email_validated,
  hashed_password,
  locale,
  picture,
  profile_link,
  auth_id,
  auth_provider,
  roles
) VALUES (
  'admin@organicity.eu',
  'administrator',
  'admin',
  'istrator',
  'femalemale',
  true,
  '$2a$10$KIYtSVGMBO4IxbF0Qj7Nauq1Ygi7fhxafAoU/eNPB39xYpBVTTKLq',
  'de_DE',
  '',
  '',
  '1',
  'password',
  'admin'
);

INSERT INTO user_linked_account (
  id,
  user_email,
  provider_key,
  provider_user_id,
  email,
  name,
  first_name,
  last_name,
  picture,
  gender,
  locale,
  profile
) VALUES (
  1,
  'admin@organicity.eu',
  'password',
  '$2a$10$KIYtSVGMBO4IxbF0Qj7Nauq1Ygi7fhxafAoU/eNPB39xYpBVTTKLq',
  'admin@organicity.eu',
  'administrator',
  'admin',
  'istrator',
  '',
  'femalemale',
  'de_DE',
  '',
);

# --- !Downs

DELETE FROM user_linked_account WHERE user_email='admin@organicity.eu';
DELETE FROM user WHERE email='admin@organicity.eu';
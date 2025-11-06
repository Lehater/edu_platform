-- === USERS (unique by email) ===
INSERT INTO users (name, email, role)
VALUES
  ('Dr. Hibernate', 'teacher@edu.local', 'TEACHER'),
  ('Alice Student', 'alice@edu.local', 'STUDENT'),
  ('Bob Student',   'bob@edu.local',   'STUDENT')
ON CONFLICT (email) DO NOTHING;

-- === CATEGORY (assume unique by name) ===
INSERT INTO categories (name) VALUES ('Programming')
ON CONFLICT (name) DO NOTHING;

-- === TAGS (assume unique by name) ===
INSERT INTO tags (name) VALUES ('Java')      ON CONFLICT (name) DO NOTHING;
INSERT INTO tags (name) VALUES ('Hibernate') ON CONFLICT (name) DO NOTHING;

-- === COURSE (insert only if not exists by title + teacher) ===
INSERT INTO courses (title, description, category_id, teacher_id, duration, start_date)
SELECT 'Hibernate Fundamentals', 'ORM basics with JPA/Hibernate',
       c.id, u.id, '4 weeks', current_date + 3
FROM categories c, users u
WHERE c.name='Programming' AND u.email='teacher@edu.local'
  AND NOT EXISTS (
      SELECT 1 FROM courses cc
      WHERE cc.title='Hibernate Fundamentals' AND cc.teacher_id = u.id
  );

-- === COURSE_TAG links (only if not exists) ===
INSERT INTO course_tag (course_id, tag_id)
SELECT crs.id, t.id
FROM courses crs, tags t
WHERE crs.title='Hibernate Fundamentals' AND t.name='Java'
  AND NOT EXISTS (SELECT 1 FROM course_tag ct WHERE ct.course_id=crs.id AND ct.tag_id=t.id);

INSERT INTO course_tag (course_id, tag_id)
SELECT crs.id, t.id
FROM courses crs, tags t
WHERE crs.title='Hibernate Fundamentals' AND t.name='Hibernate'
  AND NOT EXISTS (SELECT 1 FROM course_tag ct WHERE ct.course_id=crs.id AND ct.tag_id=t.id);

-- === MODULES (only if not exists by course+title) ===
INSERT INTO modules (course_id, title, order_index, description)
SELECT crs.id, 'Intro & JPA', 1, 'JPA 101'
FROM courses crs
WHERE crs.title='Hibernate Fundamentals'
  AND NOT EXISTS (SELECT 1 FROM modules m WHERE m.course_id = crs.id AND m.title='Intro & JPA');

INSERT INTO modules (course_id, title, order_index, description)
SELECT crs.id, 'Relations & Fetching', 2, 'OneToMany, LAZY'
FROM courses crs
WHERE crs.title='Hibernate Fundamentals'
  AND NOT EXISTS (SELECT 1 FROM modules m WHERE m.course_id = crs.id AND m.title='Relations & Fetching');

-- === LESSONS (only if not exists by module+title) ===
INSERT INTO lessons (module_id, title, content, video_url)
SELECT m.id, 'What is ORM?', 'Text content...', NULL
FROM modules m JOIN courses c ON m.course_id=c.id
WHERE c.title='Hibernate Fundamentals' AND m.title='Intro & JPA'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id=m.id AND l.title='What is ORM?');

INSERT INTO lessons (module_id, title, content, video_url)
SELECT m.id, 'Entities & IDs', 'Text content...', NULL
FROM modules m JOIN courses c ON m.course_id=c.id
WHERE c.title='Hibernate Fundamentals' AND m.title='Intro & JPA'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id=m.id AND l.title='Entities & IDs');

INSERT INTO lessons (module_id, title, content, video_url)
SELECT m.id, 'Relations', 'Text content...', NULL
FROM modules m JOIN courses c ON m.course_id=c.id
WHERE c.title='Hibernate Fundamentals' AND m.title='Relations & Fetching'
  AND NOT EXISTS (SELECT 1 FROM lessons l WHERE l.module_id=m.id AND l.title='Relations');

-- === ENROLLMENTS (unique by user_id+course_id) ===
INSERT INTO enrollments (user_id, course_id, enroll_date, status)
SELECT u.id, c.id, current_date, 'ACTIVE'
FROM users u, courses c
WHERE u.email='alice@edu.local' AND c.title='Hibernate Fundamentals'
  AND NOT EXISTS (SELECT 1 FROM enrollments e WHERE e.user_id=u.id AND e.course_id=c.id);

INSERT INTO enrollments (user_id, course_id, enroll_date, status)
SELECT u.id, c.id, current_date, 'ACTIVE'
FROM users u, courses c
WHERE u.email='bob@edu.local' AND c.title='Hibernate Fundamentals'
  AND NOT EXISTS (SELECT 1 FROM enrollments e WHERE e.user_id=u.id AND e.course_id=c.id);

-- === ASSIGNMENT (only if not exists by lesson+title) ===
INSERT INTO assignments (lesson_id, title, description, due_date, max_score, status)
SELECT l.id, 'HW #1: Map Entities', 'Create 2 entities with relation', current_date + 7, 100, 'OPEN'
FROM lessons l
WHERE l.title='Entities & IDs'
  AND NOT EXISTS (SELECT 1 FROM assignments a WHERE a.lesson_id=l.id AND a.title='HW #1: Map Entities');

-- === SUBMISSION (unique by student_id+assignment_id) ===
INSERT INTO submissions (assignment_id, student_id, submitted_at, content, score, feedback)
SELECT a.id, u.id, now(), 'My HW solution link: https://repo.local/alice/hw1', 92, 'Good job!'
FROM assignments a, users u
WHERE a.title='HW #1: Map Entities' AND u.email='alice@edu.local'
  AND NOT EXISTS (
      SELECT 1 FROM submissions s WHERE s.assignment_id=a.id AND s.student_id=u.id
  );

-- === QUIZ (1:1 with module 'Relations & Fetching') ===
INSERT INTO quizzes (module_id, title, time_limit)
SELECT m.id, 'Quiz: Relations', 20
FROM modules m JOIN courses c ON m.course_id=c.id
WHERE c.title='Hibernate Fundamentals' AND m.title='Relations & Fetching'
  AND NOT EXISTS (SELECT 1 FROM quizzes qz WHERE qz.module_id=m.id);

-- === QUESTIONS (only if not exists by quiz+text) ===
INSERT INTO questions (quiz_id, text, type)
SELECT qz.id, 'What is the default fetch for @ManyToOne in JPA spec?', 'SINGLE_CHOICE'
FROM quizzes qz WHERE qz.title='Quiz: Relations'
  AND NOT EXISTS (SELECT 1 FROM questions q WHERE q.quiz_id=qz.id AND q.text LIKE 'What is the default fetch%');

INSERT INTO questions (quiz_id, text, type)
SELECT qz.id, 'Select LAZY by default collections', 'MULTIPLE_CHOICE'
FROM quizzes qz WHERE qz.title='Quiz: Relations'
  AND NOT EXISTS (SELECT 1 FROM questions q WHERE q.quiz_id=qz.id AND q.text='Select LAZY by default collections');

-- === ANSWER OPTIONS (only if not exists by question+text) ===
-- q1 options
INSERT INTO answer_options (question_id, text, is_correct)
SELECT q.id, 'EAGER', TRUE
FROM questions q JOIN quizzes z ON q.quiz_id=z.id
WHERE z.title='Quiz: Relations' AND q.text LIKE 'What is the default fetch%'
  AND NOT EXISTS (SELECT 1 FROM answer_options ao WHERE ao.question_id=q.id AND ao.text='EAGER');

INSERT INTO answer_options (question_id, text, is_correct)
SELECT q.id, 'LAZY', FALSE
FROM questions q JOIN quizzes z ON q.quiz_id=z.id
WHERE z.title='Quiz: Relations' AND q.text LIKE 'What is the default fetch%'
  AND NOT EXISTS (SELECT 1 FROM answer_options ao WHERE ao.question_id=q.id AND ao.text='LAZY');

-- q2 options
INSERT INTO answer_options (question_id, text, is_correct)
SELECT q.id, '@OneToMany', TRUE
FROM questions q JOIN quizzes z ON q.quiz_id=z.id
WHERE z.title='Quiz: Relations' AND q.text='Select LAZY by default collections'
  AND NOT EXISTS (SELECT 1 FROM answer_options ao WHERE ao.question_id=q.id AND ao.text='@OneToMany');

INSERT INTO answer_options (question_id, text, is_correct)
SELECT q.id, '@ManyToMany', TRUE
FROM questions q JOIN quizzes z ON q.quiz_id=z.id
WHERE z.title='Quiz: Relations' AND q.text='Select LAZY by default collections'
  AND NOT EXISTS (SELECT 1 FROM answer_options ao WHERE ao.question_id=q.id AND ao.text='@ManyToMany');

INSERT INTO answer_options (question_id, text, is_correct)
SELECT q.id, '@ManyToOne', FALSE
FROM questions q JOIN quizzes z ON q.quiz_id=z.id
WHERE z.title='Quiz: Relations' AND q.text='Select LAZY by default collections'
  AND NOT EXISTS (SELECT 1 FROM answer_options ao WHERE ao.question_id=q.id AND ao.text='@ManyToOne');

-- === QUIZ SUBMISSION (allow one per (student,quiz); insert if none)
INSERT INTO quiz_submissions (quiz_id, student_id, score, taken_at)
SELECT qz.id, u.id, 100, now()
FROM quizzes qz, users u
WHERE qz.title='Quiz: Relations' AND u.email='alice@edu.local'
  AND NOT EXISTS (
      SELECT 1 FROM quiz_submissions qs WHERE qs.quiz_id=qz.id AND qs.student_id=u.id
  );

-- === REVIEWS (unique by course_id+student_id)
INSERT INTO course_reviews (course_id, student_id, rating, comment, created_at)
SELECT c.id, u.id, 5, 'Loved the content!', now()
FROM courses c, users u
WHERE c.title='Hibernate Fundamentals' AND u.email='alice@edu.local'
  AND NOT EXISTS (SELECT 1 FROM course_reviews r WHERE r.course_id=c.id AND r.student_id=u.id);

INSERT INTO course_reviews (course_id, student_id, rating, comment, created_at)
SELECT c.id, u.id, 4, 'Great intro.', now()
FROM courses c, users u
WHERE c.title='Hibernate Fundamentals' AND u.email='bob@edu.local'
  AND NOT EXISTS (SELECT 1 FROM course_reviews r WHERE r.course_id=c.id AND r.student_id=u.id);

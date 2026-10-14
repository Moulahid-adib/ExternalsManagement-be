CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================================
-- PROMPTS  (inserted first so offer/interview seeds can depend on them)
-- =====================================================================
INSERT INTO prompts (id, prompt_code, prompt_desc, schema)
SELECT uuid_generate_v4(), 'CV_EXTRACTION',
    'I''ll give you this cv/resume, please give me the candidate data, please try to put the maximum amount of data in the description fields,please take into account skill and natural languages rankings/levels could be  represented by either a star-based system or a progress bar, where the number of stars or the filled percentage of the progress bar indicates the proficiency level. The representation might be visually styled differently,but the number of stars or the progress bar''s filled portion remains the key to determining the level.It could be more complex than that, then you should interpret it visually the part around it to deduce the level.Try your best to deduce levels by any means visually contextually..',
    '{"type":"object","properties":{"fullName":{"type":"string"},"birthDate":{"type":"string","description":"the date should be in the form of YYYY-MM-DD"},"yearsOfExperience":{"type":"number"},"gender":{"type":"string","enum":["F","M"]},"summary":{"type":"string"},"mainTech":{"type":"string","description":"try to deduce the mainTech from the whole CV"},"contacts":{"type":"array","items":{"type":"object","properties":{"contactType":{"type":"string","enum":["EMAIL","PHONE","LINKEDIN"]},"contactValue":{"type":"string"}},"required":["contactType","contactValue"]}},"experiences":{"type":"array","items":{"type":"object","properties":{"companyName":{"type":"string"},"position":{"type":"string"},"startDate":{"type":"string","description":"the date should be in the form of YYYY-MM-DD"},"endDate":{"type":"string","description":"the date should be in the form of YYYY-MM-DD"},"description":{"type":"string"}}}},"skills":{"type":"array","items":{"type":"object","properties":{"skillName":{"type":"string"},"proficiencyLevel":{"type":"string","description":"default to BEGINNER","enum":["BEGINNER","INTERMEDIATE","EXPERT"]}},"required":["skillName","proficiencyLevel"]}},"educations":{"type":"array","items":{"type":"object","properties":{"institution":{"type":"string"},"diploma":{"type":"string"},"startDate":{"type":"string","description":"the date should be in the form of YYYY-MM-DD"},"endDate":{"type":"string","description":"the date should be in the form of YYYY-MM-DD"}}}},"address":{"type":"object","properties":{"country":{"type":"object","properties":{"name":{"type":"string"},"englishName":{"type":"string"}},"required":["name","englishName"]},"city":{"type":"object","properties":{"name":{"type":"string"}},"required":["name"]},"postalCode":{"type":"string","description":"try you best to get it from the whole CV"},"street":{"type":"string"},"fullAddress":{"type":"string"}},"required":["country","city","street"]},"naturalLanguages":{"type":"array","items":{"type":"object","properties":{"language":{"type":"string"},"level":{"type":"string","enum":["BEGINNER","LOWER_INTERMEDIATE","INTERMEDIATE","UPPER_INTERMEDIATE","ADVANCED"]},"englishDescription":{"type":"string"},"fullDescription":{"type":"string"},"description":{"type":"string"},"isNative":{"type":"boolean"},"languageInEnglish":{"type":"string"}},"required":["language","level","englishDescription","fullDescription","description","isNative","languageInEnglish"]}}},"required":["fullName","gender","contacts","skills","address","naturalLanguages"]}'
WHERE NOT EXISTS (SELECT 1 FROM prompts WHERE prompt_code = 'CV_EXTRACTION');

INSERT INTO prompts (id, prompt_code, prompt_desc, schema)
SELECT uuid_generate_v4(), 'INTERVIEW_EVALUATION_PROMPT',
    'You''re an expert interviewing manager and talent acquisition specialist.
            We''ve passed an interview for an #offer, to a #candidate, and we''ve gathered the information output and prepared a list of #Question/#answer from that interview,
            I will provide you below the needed information for them.
            Prepare a list of evaluations for that interview, each #evaluation_type is an entry in this list, I will also give you the list of #evaluation_types that we need to evaluate this candidate in.

            #Take in consideration these instructions:
             - The evaluations must be comprehensive and fair considering the job requirements in #offer_data and the #candidate_data
             - In relevance to the #evaluation_type being assessed, look in the #answers for technical accuracy, depth of knowledge, problem-solving approaches, and communication skills.
             - If the #answer is correct and the time of #answer is lower than the time given in the question, take it into account for positive assessment.
             - Cross-reference #candidate answers with #job requirements to ensure role-specific #evaluation.
             - Take evaluation type #coefficients in consideration
             - Ensure fairness by matching evaluation difficulty and accuracy to candidate''s stated experience level
             - Use the exact ''description'' value from each EvaluationTypes listFor the ''evaluationType'' field in the output.
             - Ensure each evaluation in the output array corresponds exactly to one EvaluationType from the input list.
             - Do not skip any evaluation types or add additional ones not provided in the Evaluation Types.
             - Reference specific technologies, skills, or experiences mentioned in the candidate profile when relevant.
             - Use simple language: A2-B1-B2
             - Return ONLY a valid JSON array with exactly this structure, no additional text or formatting:"{JSON_SCHEMA}", here you have a mock example:"{JSON_MOCK}".

            I provide bellow the needed information:
             - #Candidate Profile: "{CANDIDATE_DATA}",
             - #Job Offer requirements: "{OFFER_DATA}",
             - #Evaluation Types criteria: "{EVALUATION_TYPES_DATA}".
             - #Questions And Answers with the estimated answer time and the real answer time: "{QuestionAnswer_DATA}"',
    '[
              {
                "score": "Double - between 0.00 and 100.00",
                "feedback": "String - Try to give an overall feedback of the performance of the candidate in this evaluation type",
                "evaluationTypeDescription": "String - use the exact ''description'' field value from the corresponding EvaluationType entity"
              }
            ]'
WHERE NOT EXISTS (SELECT 1 FROM prompts WHERE prompt_code = 'INTERVIEW_EVALUATION_PROMPT');

INSERT INTO prompts (id, prompt_code, prompt_desc, schema)
SELECT uuid_generate_v4(), 'INTERVIEW_QUESTION_GENERATION_PROMPT',
    'You are an expert interviewing manager and talent acquisition specialist. We need to prepare a list of interview Questions for candidates according to the candidates'' profile, Job Offer details, and the provided evaluation types .
            I will give you the information for the candidate''s profile, job offer requirements, and evaluation types criteria, generate tailored interview questions based on it.
            Generate exactly {NUMBER_OF_QUESTIONS} interview questions.
            Take these Instructions into consideration:
            - Distribute the {NUMBER_OF_QUESTIONS} questions across all evaluation types based on their coefficient weights. Higher coefficient evaluation types should receive proportionally more questions.
            - Ensure questions align with the candidate''s experience level, main technology, and the job requirements.
            - Each question should be relevant to both candidate''s background and job requirements.
            - Assign realistic time durations (typically 2-5 minutes per question), taking into account that the duration estimated for the interview is {ESTIMATED_DURATION}.
            - Match question complexity to the candidate''s years of experience
            - Reference specific technologies, skills, or experiences mentioned in the candidate profile when relevant.
            - Use simple language: A2-B1-B2
            - Return ONLY a valid JSON array with exactly this structure, no additional text or formatting:"{JSON_SCHEMA}", here you have a mock example:"{JSON_MOCK}".
            Here below I provide the needed information:
            #Candidate Profile: "{CANDIDATE_DATA}",
            #Job Offer requirements: "{OFFER_DATA}",
            #Evaluation Types criteria: "{EVALUATION_TYPES_DATA}".',
    '[
              {
                "description": "string - The interview question text",
                "durationInMinutes": "integer - Duration in minutes (2-5)"
              }
            ]'
WHERE NOT EXISTS (SELECT 1 FROM prompts WHERE prompt_code = 'INTERVIEW_QUESTION_GENERATION_PROMPT');

INSERT INTO prompts (id, prompt_code, prompt_desc, schema)
SELECT uuid_generate_v4(), 'OFFER_FORMATTED_DESCRIPTION_EXTRACTION_PROMPT',
    'You are an expert HR data extraction specialist.
 I will give you an #Offer_Description, Extract the key information from it, and return a structured, formatted Json like provided in #JSON_SCHEMA, when extracting information, respect the schema we want an exact match, we''ll provide also a and #JSON_MOCK to help with that.
Take these instructions into consideration:
-For all the fields, keep the original text from the provided description just extract from it what matches the criteria.
-description: The main offer description and company information (keep the original text from the provided description just extract from it).
- skills: The required technical and professional skills.
 - languages: The required language names and the proficiency levels. the names should be in English, the proficiency level should be one of these words: [BEGINNER, LOWER_INTERMEDIATE ,INTERMEDIATE, UPPER_INTERMEDIATE, ADVANCED], it might not be present in the description with the exact name, detect the required language level, and convert it accordignly to one of these values.
 - yearsOfExperience: The required years of experience in the offer description (from "3-5 years" extract 3, from "5+ years" extract 5).
 - mainResponsibilities: The tasks and duties mentioned in the offer description, that the candidate will perform if he got accepted in the offer.
 - education: The required education from the offer description.
 - keywords: The keywords of the offer description.
 Return only a valid JSON object with exactly this structure, no additional text or formatting,  #JSON_SCHEMA structure: "{JSON_SCHEMA}"
 Here is an example of the expected output, #JSON_MOCK structure: "{JSON_MOCK}"
 #Offer_Description: "{OFFER_DESCRIPTION}"',
    '{
    "description": "string ",
    "mainTech": "string - Primary technology stack or focus, e.g., ''Full stack Java SpringBoot''",
    "skills": "string - Offer required skills separated by '' - '' e.g., ''Java - Spring Boot - Docker''",
    "languages": "array of objects - [{''languageName'': ''English'', ''level'': ''ADVANCED''}]",
    "yearsOfExperience": "integer - Minimum required years of experience",
    "mainResponsibilities": "string - offer tasks separated by '' - ''",
    "education": "string - Education requirements separated by '' - ''",
    "keywords": "string - Offer Keywords separated by '' - ''"
}'
WHERE NOT EXISTS (SELECT 1 FROM prompts WHERE prompt_code = 'OFFER_FORMATTED_DESCRIPTION_EXTRACTION_PROMPT');


-- countries
INSERT INTO country (id, name, english_name)
VALUES (uuid_generate_v4(), 'Maroc', 'Morocco')
    ON CONFLICT (english_name) DO NOTHING;

WITH morocco_id AS (SELECT id FROM country WHERE english_name = 'Morocco')
INSERT INTO city (id, name, country_id)
SELECT
    uuid_generate_v4(),
    city_name,
    (SELECT id FROM morocco_id)
FROM (VALUES
          ('Casablanca'), ('Rabat'), ('Marrakech'), ('Fes'), ('Tangier'),
          ('Agadir'), ('Tetouan'), ('Oujda'), ('Safi'), ('Kenitra'),
          ('Essaouira'), ('Nador'), ('Chefchaouen'), ('Meknes'), ('Salé'),
          ('Taza'), ('Khouribga'), ('El Jadida'), ('Beni Mellal'), ('Tiznit')
     ) AS cities(city_name)
    ON CONFLICT (name) DO NOTHING;

-- candidates
INSERT INTO candidates (id, full_name, birth_date, years_of_experience, gender, main_tech, summary)
SELECT
    uuid_generate_v4(),
    first_name || ' ' || last_name,
    DATE '1990-01-01' - (FLOOR(RANDOM() * 365 * 25) || ' days')::INTERVAL,
    FLOOR(RANDOM() * 7) + 2, -- 2–8 years
    gender,
    (ARRAY['Java','Python','React','Angular'])[FLOOR(RANDOM()*4)+1],
    (ARRAY[
        'Full-stack developer with strong problem-solving skills',
        'Passionate about clean code and agile methodologies',
        'DevOps specialist focused on automation and CI/CD',
        'Frontend expert with UX/UI design experience',
        'Backend engineer specializing in microservices',
        'Data-driven developer with ML experience',
        'Cloud infrastructure architect and developer',
        'Mobile developer with cross-platform expertise'
    ])[FLOOR(RANDOM()*8)+1]
FROM (
    VALUES
    ('Youssef', 'Alami', 'M'), ('Fatima', 'Zahra', 'F'), ('Mehdi', 'Benjelloun', 'M'),
    ('Nadia', 'Cherkaoui', 'F'), ('Karim', 'El Mansouri', 'M'), ('Leila', 'Bennani', 'F'),
    ('Omar', 'Tazi', 'M'), ('Samira', 'Rahmani', 'F'), ('Bilal', 'Khalil', 'M'),
    ('Hind', 'Daoudi', 'F'), ('Adil', 'Chraibi', 'M'), ('Soukaina', 'Idrissi', 'F'),
    ('Anas', 'Mourad', 'M'), ('Noura', 'Belhaj', 'F'), ('Hicham', 'Lahlou', 'M'),
    ('Asma', 'Bouzidi', 'F'), ('Rachid', 'Ouazzani', 'M'), ('Salma', 'Toumi', 'F'),
    ('Jamal', 'Zahir', 'M'), ('Imane', 'Boutaleb', 'F'), ('Nabil', 'Amrani', 'M'),
    ('Khadija', 'Safiani', 'F'), ('Yassine', 'Bakkal', 'M'), ('Hanane', 'El Kaddouri', 'F'),
    ('Reda', 'Hassani', 'M'), ('Meryem', 'Berrada', 'F'), ('Said', 'Fassi', 'M'),
    ('Najat', 'Laraki', 'F'), ('Khalid', 'Sbai', 'M'), ('Zineb', 'Mernissi', 'F'),
    ('Amine', 'Barakat', 'M'), ('Sanaa', 'Moujtahid', 'F'), ('Hamza', 'Qasri', 'M'),
    ('Ghita', 'Akkaoui', 'F'), ('Ayoub', 'Fahmi', 'M'), ('Rim', 'Mazini', 'F'),
    ('Tarik', 'Rochdi', 'M'), ('Lina', 'Benaissa', 'F'), ('Fouad', 'Temsamani', 'M'),
    ('Houda', 'Makhfi', 'F'), ('Marouane', 'Mabrouk', 'M'), ('Nihal', 'Saidi', 'F')
    ) AS names(first_name, last_name, gender);

-- address
INSERT INTO address (id, street, postal_code, full_address, city_id, country_id, candidate_id)
SELECT
    uuid_generate_v4(),
    street_type || ' ' || street_name,
    LPAD(FLOOR(RANDOM() * 100000)::TEXT, 5, '0'),
    street_type || ' ' || street_name || ', ' || c.name,
    c.id,
    (SELECT id FROM country WHERE english_name = 'Morocco'),
    cand.id
FROM candidates cand
         CROSS JOIN LATERAL (
    SELECT
        CASE
            WHEN RANDOM() < 0.3 THEN 'Rue'
            WHEN RANDOM() < 0.6 THEN 'Avenue'
            ELSE 'Boulevard'
            END AS street_type,
        (ARRAY['Mohammed V','Hassan II','Al Massira','Moulay Ismail','Oqba Ibn Nafia',
         'Mehdi Ben Barka','Abdelkrim Khattabi','Al Fadila','Ibn Sina','Al Maghrib Al Arabi'])[FLOOR(RANDOM()*10)+1] AS street_name
    ) st
    CROSS JOIN LATERAL (
SELECT id, name FROM city ORDER BY RANDOM() LIMIT 1
    ) c
WHERE NOT EXISTS (SELECT 1 FROM address a WHERE a.candidate_id = cand.id);

-- contacts
INSERT INTO contacts (id, candidate_id, contact_type, contact_value)
SELECT uuid_generate_v4(), id, 'Email',
       (ARRAY[
            'bouraouiyoussef12@gmail.com',
        'mohamedtaha.elyakoubi@nttdata.com'
            ])[FLOOR(RANDOM()*2 + 1)]
FROM candidates
UNION ALL
SELECT uuid_generate_v4(), id, 'Phone', '+2126' || LPAD(FLOOR(RANDOM()*100000000)::TEXT, 8, '0')
FROM candidates;

-- experiences
INSERT INTO experiences (id, candidate_id, company_name, position, start_date, end_date, description)
SELECT
    uuid_generate_v4(),
    id,
    (ARRAY['OCP Group','Attijariwafa Bank','Maroc Telecom','ONCF','Royal Air Maroc',
     'Saham Assurance','Yazaki Morocco','Managem','SNTL','Cosumar',
     'BMCE Bank','LafargeHolcim','Intelcia','Dell Technologies Morocco',
     'IBM Morocco','Capgemini Morocco','HPS Morocco','Inwi','Orange Morocco'])[FLOOR(RANDOM()*19)+1],
    (ARRAY['Software Engineer','Senior Developer','DevOps Specialist','Tech Lead',
           'Full Stack Developer','Data Engineer','Mobile Developer','Cloud Architect'])[FLOOR(RANDOM()*8)+1],
    CURRENT_DATE - (years_of_experience + FLOOR(RANDOM()*3) || ' years')::INTERVAL,
    CASE WHEN RANDOM() > 0.3 THEN CURRENT_DATE - (FLOOR(RANDOM()*12) || ' months')::INTERVAL END,
    (ARRAY[
        'Developed scalable solutions for enterprise clients',
        'Led team in agile development environment',
        'Implemented CI/CD pipelines and cloud infrastructure',
        'Optimized system performance and reduced costs',
        'Created mobile applications with 100k+ downloads',
        'Designed microservices architecture',
        'Mentored junior developers and conducted code reviews'
    ])[FLOOR(RANDOM()*7)+1]
FROM candidates;

-- skills
INSERT INTO skills (id, candidate_id, skill_name, proficiency_level)
WITH candidate_skills AS (
    SELECT
        id,
        (ARRAY['Java','React','Angular','Python','JavaScript','Laravel','.NET','C#'])[FLOOR(RANDOM()*8)+1] AS skill
FROM candidates
    CROSS JOIN generate_series(1, (FLOOR(RANDOM()*5)+3)::integer)
    )
SELECT
    uuid_generate_v4(),
    id,
    skill,
    CASE
        WHEN RANDOM() < 0.2 THEN 'BEGINNER'
        WHEN RANDOM() < 0.6 THEN 'INTERMEDIATE'
        ELSE 'EXPERT'
        END
FROM candidate_skills;

-- educations
INSERT INTO educations (id, candidate_id, institution, degree, start_date, end_date, diploma)
SELECT
    uuid_generate_v4(),
    id,
    (ARRAY[
         'Université Mohammed V Rabat','Université Hassan II Casablanca',
     'Université Cadi Ayyad Marrakech','Al Akhawayn University',
     'EMI Rabat','ENSET Mohammedia','EHTP Casablanca','INPT Rabat',
     'ENSIAS Rabat','Université Ibn Tofail Kénitra'
         ])[FLOOR(RANDOM()*10)+1],
    (ARRAY['Bachelor','Master','Engineering Diploma','PhD'])[FLOOR(RANDOM()*4)+1],
    CURRENT_DATE - (6 + FLOOR(RANDOM()*5) || ' years')::INTERVAL,
    CURRENT_DATE - (FLOOR(RANDOM()*3) || ' years')::INTERVAL,
    (ARRAY[
        'Computer Science','Software Engineering','Information Systems',
        'Data Science','Electrical Engineering','AI & Machine Learning'
    ])[FLOOR(RANDOM()*6)+1]
FROM candidates;

-- languages (force English + random others)
-- English (ADVANCED, native) for every candidate
INSERT INTO languages (id, candidate_id, description, english_description, full_description, language, language_in_english, level, is_native)
SELECT
    uuid_generate_v4(), id, 'English', 'English', 'English Language', 'English', 'English', 'ADVANCED', true
FROM candidates;

-- Additional random languages
INSERT INTO languages (id, candidate_id, description, english_description, full_description, language, language_in_english, level, is_native)
WITH language_data AS (
    SELECT *, ROW_NUMBER() OVER () - 1 AS row_number
    FROM (VALUES
              ('العربية', 'Arabic', 'اللغة العربية الفصحى', 'Arabic', 'Arabic'),
              ('Français', 'French', 'Langue Française', 'French', 'French'),
              ('English', 'English', 'English Language', 'English', 'English'),
              ('Español', 'Spanish', 'Idioma Español', 'Spanish', 'Spanish')
         ) AS langs (description, english_description, full_description, language, language_in_english)
),
     candidate_languages AS (
         SELECT id AS candidate_id, (FLOOR(RANDOM() * 2))::integer AS num_languages
         FROM candidates
     )
SELECT
    uuid_generate_v4(),
    c.candidate_id,
    ld.description,
    ld.english_description,
    ld.full_description,
    ld.language,
    ld.language_in_english,
    (ARRAY['BEGINNER','LOWER_INTERMEDIATE','INTERMEDIATE','UPPER_INTERMEDIATE','ADVANCED'])[FLOOR(RANDOM()*5)+1],
    (RANDOM() < 0.2)
FROM candidate_languages c
    JOIN language_data ld ON RANDOM() < 0.5
WHERE c.num_languages > 0;

-- OFFERS (aligned mainTech + biased languages)
INSERT INTO offers (id, title, description, formatted_description)
SELECT
    uuid_generate_v4(),
    (ARRAY[
         'Junior Java Developer','Senior Frontend Engineer','DevOps Specialist',
     'Full Stack Developer','Data Scientist','Mobile App Developer',
     'Cloud Solutions Architect','Backend Engineer'
         ])[FLOOR(RANDOM() * 8) + 1],
    (ARRAY[
        'Work on exciting projects with international clients.',
        'Lead frontend development teams and improve UI/UX.',
        'Manage CI/CD pipelines and automate deployments.',
        'Develop and maintain full stack applications.',
        'Analyze large datasets and build ML models.',
        'Create cross-platform mobile applications.',
        'Design and implement scalable cloud infrastructure.',
        'Build robust backend services and APIs.'
    ])[FLOOR(RANDOM() * 8) + 1],
    CAST(
        json_build_object(
            'description', 'We are seeking an experienced, highly motivated, and technically proficient Senior Java Developer to join our growing technology team. The ideal candidate will have a deep understanding of Java development and a passion for building scalable, high-performance, and reliable software applications. You will play a critical role in designing, developing, and maintaining enterprise-level solutions that power our business operations and drive innovation.
                            In this role, you will be responsible for collaborating with cross-functional teams to analyze requirements, create technical specifications, and implement end-to-end solutions. You will ensure code quality, maintainability, and adherence to best practices while mentoring junior developers and contributing to architectural decisions. Your work will directly impact the performance, scalability, and reliability of our software products.',
            'mainTech', (ARRAY['Java','Python','React','Angular'])[FLOOR(RANDOM()*4)+1],
            'skills', (ARRAY[
                'Java - Spring Boot - Docker - Kubernetes - AWS',
                'React - Node.js - MongoDB - Docker - Git',
                'Python - Django - PostgresSQL - Kubernetes - AWS',
                'C# - .NET - SQL Server - Azure - Agile'
            ])[FLOOR(RANDOM() * 4) + 1],
            'languages', json_build_array(
                json_build_object('languageName', 'English', 'level', 'INTERMEDIATE'),
                json_build_object('languageName', (ARRAY['French','Spanish'])[FLOOR(RANDOM()*2)+1], 'level', 'ADVANCED')
            ),
            'yearsOfExperience', FLOOR(RANDOM() * 5) + 2,
            'mainResponsibilities', 'Design and implement scalable systems...',
            'education', 'Bachelor in Computer Science - Master in Software Engineering',
            'keywords', 'Java - Spring Boot - Microservices - Docker - Kubernetes - Agile - DevOps - AWS - Backend Development'
        ) AS text
    )
FROM generate_series(1, 10);

-- Records
INSERT INTO recording (id, recorded_at, file_url, transcript)
SELECT
    uuid_generate_v4(),
    CURRENT_TIMESTAMP - (FLOOR(RANDOM() * 30) || ' days')::INTERVAL,
    'https://storage.example.com/recordings/' || uuid_generate_v4()::TEXT || '.mp3',
    '[' || string_agg(
            json_build_object(
                    'Question', questions[FLOOR(RANDOM() * array_length(questions,1) + 1)::INT],
                    'QuestionTime', (FLOOR(RANDOM() * 60) || ':' || FLOOR(RANDOM() * 60) || ':' || FLOOR(RANDOM() * 60)),
                    'Answer', answers[FLOOR(RANDOM() * array_length(answers,1) + 1)::INT],
                    'AnswerTime', (FLOOR(RANDOM() * 60) || ':' || FLOOR(RANDOM() * 60) || ':' || FLOOR(RANDOM() * 60))
            )::TEXT,
            ','
           ) || ']'
FROM generate_series(1,20) AS gs,
     LATERAL (
              SELECT ARRAY[
                         'What is your name?',
                     'Where are you from?',
                     'What do you do?',
                     'How was your day?',
                     'Describe your experience.',
                     'Tell me about your hobbies.',
                     'What is your favorite book?',
                     'What are your goals?'
    ] AS questions,
    ARRAY[
    'My name is John.',
    'I am from Morocco.',
    'I work as an engineer.',
    'It was great!',
    'I had an amazing experience.',
    'I enjoy reading.',
    'I love science fiction.',
    'My goal is to become a developer.'
    ] AS answers
    ) AS q_and_a;


-- INTERVIEWS - FIXED VERSION WITH UNIQUE DESCRIPTIONS
WITH offer_candidates AS (
    SELECT
    o.id AS offer_id,
    o.title AS offer_title,
    c.id AS candidate_id,
    c.full_name AS candidate_name,
    ROW_NUMBER() OVER (ORDER BY RANDOM()) AS rn
    FROM offers o
    CROSS JOIN candidates c
    ORDER BY RANDOM()
    LIMIT 20
    ),
    interview_details AS (
    SELECT
    oc.*,
    CURRENT_TIMESTAMP - (FLOOR(RANDOM() * 30) || ' days')::INTERVAL AS starttime,
    (30 + FLOOR(RANDOM() * 60)) * INTERVAL '1 minute' AS duration,
    r.id AS recording_id,
    CASE
    WHEN oc.rn % 8 = 1 THEN 'Technical Interview - ' || oc.offer_title || ' position with ' || oc.candidate_name
    WHEN oc.rn % 8 = 2 THEN 'First Round Interview - ' || oc.candidate_name || ' for ' || oc.offer_title
    WHEN oc.rn % 8 = 3 THEN 'HR Screening - ' || oc.offer_title || ' candidate assessment'
    WHEN oc.rn % 8 = 4 THEN 'Final Interview - ' || oc.candidate_name || ' final evaluation'
    WHEN oc.rn % 8 = 5 THEN 'Phone Interview - Initial screening for ' || oc.offer_title
    WHEN oc.rn % 8 = 6 THEN 'Panel Interview - ' || oc.candidate_name || ' team assessment'
    WHEN oc.rn % 8 = 7 THEN 'Behavioral Interview - ' || oc.offer_title || ' cultural fit evaluation'
    ELSE 'Follow-up Interview - ' || oc.candidate_name || ' second round discussion'
    END AS interview_description,
    (ARRAY[
    'Candidate showed strong technical skills and communication',
    'Good cultural fit, needs technical assessment follow-up',
    'Excellent problem-solving abilities demonstrated',
    'Strong background but limited in required technology',
    'Impressive portfolio and project experience',
    'Good teamwork skills, moderate technical knowledge',
    'Outstanding communication and leadership potential',
    'Solid technical foundation with room for growth',
    'Exceptional analytical thinking and creativity',
    'Great enthusiasm and willingness to learn'
    ])[FLOOR(RANDOM() * 10) + 1] AS interview_comment
    FROM offer_candidates oc
    CROSS JOIN LATERAL (
    SELECT id FROM recording ORDER BY RANDOM() LIMIT 1
    ) r
    ),
    dynamic_transcription AS (
    SELECT
    t.g,
    -- AI Question (Dynamic Time)
    'AI - 00:' || LPAD((FLOOR(RANDOM() * 20) + 1)::TEXT, 2, '0') || ':00 : ' ||
    (ARRAY[
    'Could you explain the concept of **Polymorphism** in Java and give a real-world example?',
    'Describe a scenario where you would use the **Singleton design pattern** versus a Factory pattern.',
    'What are the performance implications when choosing between **String, StringBuilder, and StringBuffer**?',
    'Walk me through the lifecycle of a **Spring bean** and point out where dependency injection occurs.',
    'Explain how **Garbage Collection** works in the JVM and what Generational Garbage Collection achieves.',
    'How do you manage configuration secrets and credentials in a **Kubernetes** environment?',
    'What is the primary difference between **IaaS, PaaS, and SaaS**, and which do you prefer for application deployment?',
    'Describe your experience with **CI/CD pipelines** using tools like Jenkins or GitLab CI.',
    'When scaling microservices, what strategies do you employ for **service discovery and load balancing**?',
    'What are the advantages of using a **message broker** like Kafka or RabbitMQ in a distributed system?',
    'What is the difference between a **JOIN and an ACID transaction** in PostgreSQL?',
    'Explain the concept of **indexing** in a relational database and how to decide which columns to index.',
    'Describe how you handle large dataset migrations with **zero downtime**.',
    'What are the benefits and drawbacks of using a **NoSQL database** like MongoDB over a traditional SQL database?',
    'How do you optimize slow-running database queries and identify bottlenecks?',
    'Describe a time you had to **mentor a junior developer** on a complex technical issue.',
    'How do you ensure the **security** of the code you write from a high-level perspective?',
    'What is the most complex **legacy code** base you have had to work with, and how did you approach it?',
    'Explain the process of **Test-Driven Development (TDD)** and how it impacts your workflow.',
    'Where do you see yourself in five years, especially concerning **cloud architecture**?'
    ])[FLOOR(RANDOM()*20)+1] || ' || Candidate - 00:' || LPAD((FLOOR(RANDOM() * 20) + 1)::TEXT, 2, '0') || ':30 : ' ||
    -- Candidate Answer (Dynamic Time)
    (ARRAY[
    'Polymorphism allows objects of different classes to be treated as objects of a common type. A good example is using a list of a parent class type to hold various sub-class objects and iterating through them.',
    'The Singleton is best for resources like database connection pools or logging, guaranteeing a single instance. Factory is for abstracting object creation, giving clients flexibility without exposing instantiation logic.',
    'String is immutable, leading to memory overhead with constant concatenation. StringBuilder is faster and mutable but not thread-safe; StringBuffer is thread-safe but slightly slower. I use StringBuilder in single-threaded environments.',
    'The bean lifecycle starts with instantiation, followed by dependency injection (via setters/constructors), initialization callbacks (@PostConstruct), and finally, it’s ready for use. It ends with the container closing and destruction callbacks.',
    'GC tracks and removes unreferenced objects. Generational GC segments the heap into young and old generations, which significantly optimizes performance because most objects die young, avoiding expensive full collections.',
    'I use **Kubernetes Secrets** for sensitive data, coupled with external solutions like HashiCorp Vault or cloud-native key management services for better protection and rotation.',
    'I prefer **PaaS (Platform as a Service)**, like AWS Elastic Beanstalk or Azure App Service, as it balances control with productivity, handling the underlying OS/infrastructure updates for me. IaaS is too much manual overhead.',
    'I have designed Jenkins pipelines that include static code analysis (SonarQube), unit/integration testing, and deployment to staging environments, all triggered automatically on every pull request merge.',
    'I use a **service mesh** like Istio or Consul for automated service discovery, coupled with Round Robin or Least Connections algorithms for traffic distribution at the ingress level.',
    'Message brokers are vital for **decoupling services** and handling asynchronous communication, ensuring reliability for tasks like order fulfillment or notification processing, which don''t require immediate synchronous confirmation.',
    'A **JOIN** is a standard SQL operation to combine columns from two or more rows based on a related column. An **ACID transaction** is a fundamental concept guaranteeing that database operations are reliable and complete as a single unit.',
    'Indexing significantly speeds up search queries on large tables but slows down writes (INSERT/UPDATE). I index columns frequently used in `WHERE` clauses, `ORDER BY`, or `JOIN` conditions, but avoid over-indexing, especially on low-cardinality columns.',
    'I use a **shadow database strategy** or **logical replication**. For shadow databases, the new schema is built alongside the old, and data is double-written during the transition phase, ensuring seamless cutover.',
    'NoSQL is great for **high-velocity, schema-less data** like session logs or user profiles, offering horizontal scalability. SQL is better when data integrity (ACID) and complex relational querying are non-negotiable requirements.',
    'I start by running an `EXPLAIN ANALYZE` on the query to understand its execution plan, looking for **full table scans** or missing indexes. Often, rewriting a subquery or adjusting an index solves the issue.',
    'I use a **pair programming** approach initially, guiding them through debugging without giving them the direct answer. I emphasize the thought process and resource utilization over the immediate fix, building long-term competence.',
    'Security is a layered approach. I start with **OWASP Top 10** checks, use static analysis tools in the pipeline, and ensure all input is validated and sanitized, following the principle of least privilege in access controls.',
    'I dealt with a monolithic banking system using Java 6 and legacy EJBs. My approach was to implement an **anti-corruption layer** around key business domains, allowing us to slowly extract services into a modern framework without halting feature development.',
    'TDD forces me to think about the **contract and interface** before implementation. It results in code that is inherently more testable, modular, and leads to fewer production bugs because the requirements are enshrined in automated tests.',
    'I aim to transition into a **Principal or Lead Architect** role, focusing on designing highly available, multi-region cloud systems, specifically leveraging serverless technologies and advanced networking on AWS or Azure.'
    ])[FLOOR(RANDOM()*20)+1] AS line
    FROM generate_series(1, 25) AS t(g)
    ),
    transcription_json AS (
    SELECT json_agg(line) AS transcription FROM dynamic_transcription
    )
INSERT INTO interviews (
    id,
    offer_id,
    candidate_id,
    starttime,
    endtime,
    description,
    link,
    scheduled_at,
    comment,
    number_of_questions,
    estimated_duration,
    recording_id,
    transcription
)
SELECT
    gen_random_uuid(),
    i.offer_id,
    i.candidate_id,
    i.starttime,
    i.starttime + i.duration AS endtime,
    i.interview_description,
    'https://zoom.us/j/' || FLOOR(RANDOM() * 1000000000)::TEXT AS link,
            CURRENT_TIMESTAMP AS scheduled_at,
    i.interview_comment,
    FLOOR(RANDOM() * 10) + 5 AS number_of_questions,
    EXTRACT(EPOCH FROM i.duration) / 60 AS estimated_duration,
    i.recording_id,
    (SELECT transcription FROM transcription_json)
FROM interview_details i;

-- EVALUATION_TYPES (Including OverAll)
INSERT INTO evaluation_types (id, description, coefficient)
SELECT
    uuid_generate_v4(),
    description,
    CASE description
        WHEN 'Technical Skills' THEN 3
        WHEN 'Communication' THEN 2
        WHEN 'Problem Solving' THEN 3
        WHEN 'Cultural Fit' THEN 1
        WHEN 'Experience' THEN 2
        WHEN 'Motivation' THEN 1
        WHEN 'OverAll' THEN 4
        ELSE 1
        END
FROM (VALUES
          ('Technical Skills'),
          ('Communication'),
          ('Problem Solving'),
          ('Cultural Fit'),
          ('Experience'),
          ('Motivation'),
          ('OverAll')
     ) AS t(description);

-- EVALUATIONS
WITH interview_evaluation_combinations AS (
    SELECT
        i.id as interview_id,
        et.id as evaluation_type_id,
        et.description as eval_type
    FROM interviews i
             CROSS JOIN evaluation_types et
)
INSERT INTO evaluations (id, score, feedback, interview_id, evaluation_type_id)
SELECT
    uuid_generate_v4(),
    CASE
        WHEN iec.eval_type = 'Technical Skills' THEN ROUND((RANDOM() * 40 + 40)::NUMERIC, 2)  -- 40-80 range
        WHEN iec.eval_type = 'Communication' THEN ROUND((RANDOM() * 30 + 50)::NUMERIC, 2)     -- 50-80 range
        WHEN iec.eval_type = 'Problem Solving' THEN ROUND((RANDOM() * 35 + 45)::NUMERIC, 2)   -- 45-80 range
        ELSE ROUND((RANDOM() * 40 + 50)::NUMERIC, 2)  -- 50-90 range for other skills
        END,
    CASE iec.eval_type
        WHEN 'Technical Skills' THEN
            (ARRAY[
                 'Demonstrated strong proficiency in Java and Spring Boot framework. Showcased solid understanding of dependency injection, AOP, and RESTful API design. Able to discuss design patterns and their applications. However, questions about advanced Spring Security configurations revealed gaps that could be addressed with experience.',
             'Good understanding of microservices architecture and distributed systems. Explained service decomposition strategies, API gateway patterns, and inter-service communication effectively. Showed familiarity with Docker and container orchestration. Would benefit from hands-on experience with service mesh technologies and distributed transactions.',
             'Technical knowledge is solid but needs improvement in database design and optimization. Understood basic SQL operations and normalization but struggled with complex query optimization and indexing strategies. Recommend training in database performance tuning and execution plans before handling production systems.',
             'Excellent coding practices and clean code principles. Wrote well-structured, readable code during the assessment. Demonstrated understanding of SOLID principles, proper naming conventions, and code organization. Their approach to refactoring legacy code showed maturity and attention to maintainability.',
             'Limited experience with cloud technologies, particularly AWS services. Has theoretical knowledge of cloud computing but lacks practical implementation with EC2, S3, Lambda, and RDS. This gap could be bridged through targeted training and guided project work in the first few months.',
             'Outstanding debugging and problem-solving skills throughout the technical interview. Systematically approached issues, used effective debugging techniques, and showed strong logical reasoning. Able to identify root causes quickly and propose multiple solution approaches. This is a standout strength.',
             'Good grasp of testing frameworks including JUnit, Mockito, and TDD principles. Wrote meaningful unit tests and understood the importance of test coverage. Demonstrated knowledge of integration testing and mocking strategies. Could improve in test-driven development discipline and writing tests first.',
             'Needs more experience with DevOps practices and CI/CD pipelines. Familiar with Git and version control but has limited exposure to Jenkins, GitLab CI, or GitHub Actions. Understanding of infrastructure as code and deployment automation is basic. Mentoring in DevOps culture would be beneficial.'
                 ])[FLOOR(RANDOM() * 8) + 1]
        WHEN 'Communication' THEN
            (ARRAY[
                 'Excellent verbal communication skills demonstrated throughout the interview. Articulated thoughts clearly and confidently, maintained good eye contact, and showed enthusiasm discussing technical topics. Adapted communication style appropriately when explaining complex concepts to non-technical panel members.',
             'Provided clear and concise explanations without unnecessary jargon. Has the ability to break down complex technical problems into understandable components. Responded to questions directly and stayed on topic. Their communication style would work well in client-facing situations and cross-functional collaboration.',
             'Demonstrated good active listening abilities and emotional intelligence. Waited for questions to be completed before responding, asked thoughtful follow-up questions, and showed understanding of what was asked. Acknowledged when they didn''t understand something rather than making assumptions, which shows maturity.',
             'Needs improvement in presentation skills and structuring longer responses. While they know the material well, explanations sometimes wandered or lacked clear structure. Would benefit from practicing the STAR method for behavioral questions and organizing technical explanations with clear structure.',
             'Strong interpersonal skills and natural rapport-building ability. Was personable, showed genuine interest in the team and company culture, and engaged in meaningful dialogue beyond answering questions. Demonstrated humor and warmth while maintaining professionalism. Would be an asset to team dynamics.',
             'Confident and articulate responses showing strong command of technical vocabulary. Spoke with authority on their areas of expertise without appearing arrogant. Admitted knowledge gaps honestly and showed curiosity about learning. Their confidence would likely inspire trust with stakeholders and team members.',
             'Good at asking clarifying questions before diving into answers. Didn''t make assumptions and sought to understand the full context of problems presented. This shows analytical thinking and desire to provide accurate responses. Such an approach would be valuable in requirements gathering and client interactions.',
             'Effective in explaining technical concepts to diverse audiences. Demonstrated ability to adjust their language and level of detail based on the audience. When asked to explain a technical topic to a business stakeholder, successfully removed jargon and focused on business value and outcomes.'
                 ])[FLOOR(RANDOM() * 8) + 1]
        WHEN 'Problem Solving' THEN
            (ARRAY[
                 'Demonstrated systematic approach to problem solving during the assessment. Broke down complex problems into smaller, manageable pieces and tackled them methodically. Documented their thought process clearly and considered edge cases. Their structured approach would be valuable for handling complex production issues.',
             'Shows creative thinking and proposes innovative solutions to challenges. Didn''t just rely on standard approaches but thought outside the box when presented with constraints. Suggested alternative solutions and weighed trade-offs effectively. This creative problem-solving would be beneficial for architecture discussions and optimization tasks.',
             'Good analytical and logical reasoning skills evident in coding exercises. Approached problems logically, identified patterns, and applied appropriate algorithms. Explained their reasoning clearly before coding and adjusted approach based on feedback. Strong foundation in computer science fundamentals supports their problem-solving ability.',
             'Needs to improve at breaking down complex problems into smaller components. Sometimes tried to solve everything at once rather than decomposing the problem. This led to confusion and backtracking during the coding assessment. With practice in systematic problem decomposition, they could significantly improve effectiveness.',
             'Strong debugging and troubleshooting skills demonstrated with real-world scenarios. When presented with a bug in existing code, used systematic debugging techniques, formed hypotheses, and tested them methodically. Didn''t jump to conclusions and thoroughly verified fixes. This analytical approach to debugging is exactly what we need.',
             'Good at identifying root causes rather than treating symptoms. Showed depth in their analysis and didn''t stop at surface-level solutions. Asked probing questions to understand underlying issues and considered long-term implications. This mindset would help prevent recurring problems in production systems.',
             'Effective in handling challenging scenarios and pressure situations. When given a time-boxed problem-solving exercise, remained calm, prioritized effectively, and delivered a working solution. Communicated progress and didn''t panic when encountering obstacles. This composure under pressure is crucial for incident response.',
             'Quick learner with good adaptation skills when introduced to unfamiliar concepts. When presented with a technology they hadn''t used, quickly grasped key concepts and applied them appropriately. Asked insightful questions and made connections to similar technologies they knew. This learning agility is highly valuable.'
                 ])[FLOOR(RANDOM() * 8) + 1]
        WHEN 'Cultural Fit' THEN
            (ARRAY[
                 'Great team player with collaborative spirit and positive energy. Emphasized teamwork in their examples, showed appreciation for diverse perspectives, and demonstrated conflict resolution skills. Expressed genuine interest in mentoring junior developers and learning from senior team members. Their collaborative mindset aligns perfectly with our team-first culture.',
             'Strong alignment with company values, particularly around innovation and customer focus. Career choices and project selections demonstrate values consistent with ours. Spoke passionately about delivering value to end users and showed understanding of balancing technical excellence with business needs. Cultural alignment appears very strong.',
             'Good cultural fit for our agile environment and iterative development approach. Has experience with agile methodologies, understands the importance of feedback loops, and embraces change. Expressed comfort with ambiguity and iterative refinement. Their mindset matches our fast-paced, adaptive work environment well.',
             'Positive attitude and growth mindset evident throughout the conversation. Views challenges as learning opportunities and spoke enthusiastically about areas where they want to grow. Didn''t make excuses for gaps in knowledge but instead expressed excitement about learning. This attitude would contribute positively to team morale.',
             'Strong work ethic and dedication to quality demonstrated through examples. Shared stories of going above and beyond to deliver excellent results, taking ownership of problems, and following through on commitments. Their professionalism and reliability would make them a dependable team member.',
             'Good fit for our remote/hybrid work culture with strong self-management skills. Has experience working remotely, demonstrated good communication practices for distributed teams, and showed initiative in staying connected with teammates. Understands the importance of overcommunication and documentation in remote settings.',
             'Excellent interpersonal skills that would enhance team dynamics. Is approachable, empathetic, and showed emotional intelligence in their responses. Demonstrated ability to build relationships, give and receive feedback constructively, and navigate interpersonal challenges. Would be a positive influence on team culture.',
             'Shows initiative and proactive approach to problem-solving and improvement. Provided examples of identifying issues before they became critical, suggesting process improvements, and taking ownership beyond immediate responsibilities. This proactive mindset aligns with our culture of continuous improvement and ownership.'
                 ])[FLOOR(RANDOM() * 8) + 1]
        WHEN 'Experience' THEN
            (ARRAY[
                 'Solid experience in enterprise applications with exposure to complex business domains. Has worked on large-scale systems serving thousands of users, dealt with enterprise integration challenges, and understands compliance requirements. Their experience with enterprise patterns would be immediately applicable to our projects.',
             'Good background in startup environment showing adaptability and versatility. Has worn multiple hats, worked in fast-paced settings with limited resources, and delivered features quickly. Understands the trade-offs between speed and perfection. This startup experience has made them resourceful and comfortable with ambiguity.',
             'Relevant project experience in similar domains, particularly in e-commerce and payment processing. Has implemented shopping carts, payment gateway integrations, and order management systems. This domain knowledge would significantly reduce their ramp-up time and allow meaningful contributions from day one.',
             'Limited professional experience but shows promising career progression and continuous learning. Has made smart career moves, consistently taken on more responsibility, and actively sought learning opportunities. While they lack senior-level experience, their trajectory suggests they will reach that level quickly with mentoring.',
             'Strong portfolio of completed projects demonstrating end-to-end ownership. Has shipped multiple projects from conception to production, handled deployment and monitoring, and maintained systems post-launch. This full-cycle experience shows they understand all phases of software development lifecycle.',
             'Good mix of frontend and backend experience with full-stack capabilities. Is comfortable working across the stack, from database design through API development to UI implementation. While stronger on the backend, their frontend skills are sufficient for full-stack collaboration. This versatility would be valuable for cross-functional teams.',
             'Valuable experience in team leadership and mentoring junior developers. Has led small teams, conducted code reviews, mentored interns, and helped establish development practices. Their leadership experience, even if informal, would be beneficial as we''re looking for someone who can grow into a tech lead role.',
             'Impressive internship and academic projects showing strong foundation and passion. While early in their career, has built substantial projects during studies and internships. Contributed to open-source, participated in hackathons, and pursued self-directed learning. This initiative and passion suggest high potential for growth.'
                 ])[FLOOR(RANDOM() * 8) + 1]
        ELSE
            (ARRAY[
                 'Highly motivated and enthusiastic about the role and company mission. Did thorough research about our products, asked insightful questions about our roadmap, and expressed genuine excitement about potential contributions. Their enthusiasm is infectious and would bring positive energy to the team.',
             'Strong desire for continuous learning and professional development. Actively pursues learning through online courses, technical books, conferences, and side projects. Articulated clear learning goals and showed openness to feedback. This commitment to growth aligns well with our investment in employee development.',
             'Clear career goals and aspirations that align with our growth opportunities. Wants to develop into a technical architect role and has mapped out skills they need to develop. Our team structure and projects offer the right path for this progression. The alignment between their goals and our opportunities is excellent.',
             'Good understanding of role expectations and realistic about challenges. Asked thoughtful questions about the role, understood the technical challenges ahead, and showed awareness of areas where they''d need to grow. This realistic self-assessment and understanding of expectations bodes well for successful onboarding.',
             'Passionate about technology and innovation with genuine curiosity. Stays current with industry trends, experiments with new technologies in personal projects, and thinks critically about tech adoption. Brought up interesting technical discussions during the interview showing depth of interest beyond job requirements.',
             'Shows commitment to professional growth and taking ownership of career development. Has sought out mentors, participated in professional communities, and invested personal time in skill development. Views their career as a journey they''re actively managing rather than something that happens to them.',
             'Eager to contribute to team success and make immediate impact. Asked about onboarding process, how they could add value quickly, and showed genuine interest in understanding team challenges. Their focus on contribution rather than personal benefit is refreshing and indicates strong team orientation.',
             'Demonstrates self-motivation and internal drive for excellence. Provided examples of going beyond requirements, pursuing excellence even when not required, and pushing themselves to improve. This intrinsic motivation is more valuable than external pressure and suggests they''ll thrive in our autonomous work environment.'
                 ])[FLOOR(RANDOM() * 8) + 1]
        END,
    iec.interview_id,
    iec.evaluation_type_id
FROM interview_evaluation_combinations iec;
-- QUESTIONS - Create varied questions for each interview
WITH interview_question_pools AS (
    SELECT
        i.id as interview_id,
        o.title as position_title,
        -- Different question sets based on position
        CASE
            WHEN o.title LIKE '%Java%' OR o.title LIKE '%Backend%' THEN
                ARRAY[
                    'Explain the difference between String, StringBuilder, and StringBuffer in Java',
                    'How do you handle exceptions in Spring Boot applications?',
                    'Describe your experience with microservices architecture',
                    'What is dependency injection and how does Spring implement it?',
                    'How do you optimize database queries in your applications?',
                    'Explain the concept of RESTful web services',
                    'How do you implement security in Spring Boot applications?',
                    'Describe your testing strategy for backend applications'
                ]
            WHEN o.title LIKE '%Frontend%' OR o.title LIKE '%React%' OR o.title LIKE '%Angular%' THEN
                ARRAY[
                    'Explain the virtual DOM concept in React',
                    'How do you manage state in large React applications?',
                    'What are React hooks and how do you use them?',
                    'Describe your approach to responsive web design',
                    'How do you optimize frontend application performance?',
                    'Explain the difference between controlled and uncontrolled components',
                    'How do you handle API integration in frontend applications?',
                    'Describe your experience with CSS preprocessors and frameworks'
                ]
            WHEN o.title LIKE '%DevOps%' THEN
                ARRAY[
                    'Explain the CI/CD pipeline you have implemented',
                    'How do you monitor application performance in production?',
                    'Describe your experience with containerization using Docker',
                    'How do you implement infrastructure as code?',
                    'What is your approach to automated testing in DevOps?',
                    'Explain blue-green deployment strategy',
                    'How do you handle secrets management in applications?',
                    'Describe your experience with cloud platforms (AWS, Azure, GCP)'
                ]
            WHEN o.title LIKE '%Full Stack%' THEN
                ARRAY[
                    'Describe a full-stack application you have developed',
                    'How do you ensure consistency between frontend and backend?',
                    'Explain your database design approach for web applications',
                    'How do you handle authentication and authorization?',
                    'Describe your API design principles',
                    'How do you manage data flow in full-stack applications?',
                    'What is your approach to code organization in full-stack projects?',
                    'How do you handle real-time features in web applications?'
                ]
            ELSE
                ARRAY[
                    'Tell me about a challenging project you worked on',
                    'How do you stay updated with new technologies?',
                    'Describe a time when you had to debug a complex issue',
                    'How do you approach code reviews?',
                    'What is your experience working in agile environments?',
                    'How do you handle tight deadlines and pressure?',
                    'Describe your collaboration style with team members',
                    'What motivates you in software development?'
                ]
        END as question_pool
    FROM interviews i
    JOIN offers o ON i.offer_id = o.id
),
interview_questions AS (
    SELECT
        iqp.interview_id,
        iqp.question_pool[FLOOR(RANDOM() * array_length(iqp.question_pool, 1)) + 1] as question_text,
        generate_series(1, (FLOOR(RANDOM() * 4) + 3)::integer) as question_num  -- 3-6 questions per interview
    FROM interview_question_pools iqp
)
INSERT INTO questions (id, description, duration_in_minutes, interview_id, answer_id)
SELECT
    uuid_generate_v4(),
    iq.question_text,
    (ARRAY[5, 8, 10, 12, 15, 20])[FLOOR(RANDOM() * 6) + 1],  -- Varied duration
    iq.interview_id,
    NULL  -- No answer assigned initially
FROM interview_questions iq;
commend:
- run
docker run --name medapp_container -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=medapp_db -p 5432:5432 -d postgres
- stop/remove container
docker rm -f medapp_container
- enter db (from root)
docker exec -it medapp_container psql -U postgres -d medapp_db
- logs
docker logs medapp_container
- stop
docker stop medapp_container
-start 
docker start medapp_container

postgres in system:
sudo systemctl stop postgresql
sudo systemctl disable postgresql

unused dependencies:
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>


used:
- for api
		<dependency>
    		<groupId>org.springdoc</groupId>
    		<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    		<version>2.2.0</version>
		</dependency>


repair:
rm -rf target
pkill -f java
mvn clean spring-boot:run
mvn clean spring-boot:run -DskipTests

repair when accidentally run from root:
killall -9 java
su -
cd /home/maja/school/projects/med_app
rm -rf target
chown -R maja: .
mvn clean install
mvn spring-boot:run

ANGULAR:
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
apt install -y nodejs
apt install npm
npm install -g @angular/cli

cd ~/school/projects/
ng new med-app-frontend


cd med-app-frontend/
code .
ng serve








DB
-- 1. CZYSZCZENIE TABEL (Wyczyść stare dane)
-- Używamy TRUNCATE z CASCADE, co jest szybsze i czyści powiązania w Postgresie
TRUNCATE TABLE appointment, patient, doctor RESTART IDENTITY CASCADE;

-- Alternatywnie, jeśli TRUNCATE robi problemy, użyj klasycznych DELETE:
-- DELETE FROM appointment;
-- DELETE FROM patient;
-- DELETE FROM doctor;


-- 2. DODAWANIE LEKARZY (10 rekordów - English)
INSERT INTO doctor (id, first_name, last_name, specialization) VALUES
(1, 'Gregory', 'House', 'Diagnostician'),
(2, 'James', 'Wilson', 'Oncologist'),
(3, 'Lisa', 'Cuddy', 'Endocrinologist'),
(4, 'Eric', 'Foreman', 'Neurologist'),
(5, 'Allison', 'Cameron', 'Immunologist'),
(6, 'Robert', 'Chase', 'Surgeon'),
(7, 'Chris', 'Taub', 'Plastic Surgeon'),
(8, 'Remy', 'Hadley', 'Internist'),
(9, 'Lawrence', 'Kutner', 'Sports Medicine'),
(10, 'Amber', 'Volakis', 'Radiologist');


-- 3. DODAWANIE PACJENTÓW (10 rekordów - English)
INSERT INTO patient (id, first_name, last_name, pesel, disease, main_doctor_id) VALUES
(1, 'John', 'Smith', '85010112345', 'Flu', 1),
(2, 'Emily', 'Johnson', '92030354321', 'Migraine', 4),
(3, 'Michael', 'Williams', '78112267890', 'Diabetes', 3),
(4, 'Sarah', 'Brown', '65050511223', 'Hypertension', 2),
(5, 'David', 'Jones', '99121209876', 'Broken leg', 6),
(6, 'Jennifer', 'Garcia', '88070733445', 'Allergy', 5),
(7, 'James', 'Miller', '75090966778', 'Back pain', 1),
(8, 'Elizabeth', 'Davis', '95021455667', 'Sore throat', 8),
(9, 'Robert', 'Rodriguez', '82042399887', 'Arrhythmia', 2),
(10, 'Mary', 'Martinez', '60010122334', 'Rheumatism', 1);


-- 4. DODAWANIE WIZYT (12 rekordów na tydzień 9-13 Feb 2026)
-- Format daty ISO: 'YYYY-MM-DDTHH:MM:SS'

-- Monday 09.02
INSERT INTO appointment (visit_time, doctor_id, patient_id, deleted) VALUES 
('2026-02-09T08:00:00', 1, 1, false), -- Dr House (John Smith)
('2026-02-09T08:15:00', 1, 7, false), -- Dr House (James Miller)
('2026-02-09T09:00:00', 2, 4, false), -- Dr Wilson
('2026-02-09T10:30:00', 4, 2, false); -- Dr Foreman

-- Tuesday 10.02
INSERT INTO appointment (visit_time, doctor_id, patient_id, deleted) VALUES 
('2026-02-10T08:30:00', 3, 3, false), -- Dr Cuddy
('2026-02-10T12:00:00', 6, 5, false); -- Dr Chase

-- Wednesday 11.02
INSERT INTO appointment (visit_time, doctor_id, patient_id, deleted) VALUES 
('2026-02-11T14:00:00', 5, 6, false), -- Dr Cameron
('2026-02-11T14:15:00', 5, 9, false); -- Dr Cameron (Consultation)

-- Thursday 12.02
INSERT INTO appointment (visit_time, doctor_id, patient_id, deleted) VALUES 
('2026-02-12T09:15:00', 1, 10, false), -- Dr House
('2026-02-12T11:00:00', 8, 8, false);  -- Dr Hadley

-- Friday 13.02
INSERT INTO appointment (visit_time, doctor_id, patient_id, deleted) VALUES 
('2026-02-13T08:00:00', 2, 9, false),  -- Dr Wilson
('2026-02-13T15:45:00', 7, 3, false);  -- Dr Taub

-- Poniedziałek 16.02
INSERT INTO appointment (visit_time, doctor_id, patient_id, deleted) VALUES 
('2026-02-16T08:30:00', 1, 2, false),  -- Dr House (Emily Johnson)
('2026-02-16T10:00:00', 3, 5, false),  -- Dr Cuddy (David Jones)
('2026-02-16T15:00:00', 7, 1, false);  -- Dr Taub (John Smith)

-- Wtorek 17.02
INSERT INTO appointment (visit_time, doctor_id, patient_id, deleted) VALUES 
('2026-02-17T09:00:00', 2, 8, false),  -- Dr Wilson (Elizabeth Davis)
('2026-02-17T11:15:00', 4, 3, false),  -- Dr Foreman (Michael Williams)
('2026-02-17T11:30:00', 4, 7, false);  -- Dr Foreman (James Miller) - krótka wizyta

-- Środa 18.02
INSERT INTO appointment (visit_time, doctor_id, patient_id, deleted) VALUES 
('2026-02-18T13:00:00', 5, 6, false),  -- Dr Cameron (Jennifer Garcia)
('2026-02-18T14:45:00', 10, 9, false); -- Dr Volakis (Robert Rodriguez)

-- Czwartek 19.02
INSERT INTO appointment (visit_time, doctor_id, patient_id, deleted) VALUES 
('2026-02-19T08:00:00', 6, 4, false),  -- Dr Chase (Sarah Brown)
('2026-02-19T09:30:00', 8, 10, false); -- Dr Hadley (Mary Martinez)

-- Piątek 20.02
INSERT INTO appointment (visit_time, doctor_id, patient_id, deleted) VALUES 
('2026-02-20T10:00:00', 9, 5, false),  -- Dr Kutner (David Jones)
('2026-02-20T12:15:00', 2, 1, false);  -- Dr Wilson (John Smith)

-- 5. NAPRAWA LICZNIKÓW ID (Dla PostgreSQL)
-- To jest kluczowe! Ponieważ wpisaliśmy ID ręcznie (1, 2, 3...), 
-- Postgres "myśli", że licznik nadal jest na 1. 
-- Te komendy ustawią licznik na najwyższą wartość + 1.

SELECT setval(pg_get_serial_sequence('doctor', 'id'), (SELECT MAX(id) FROM doctor));
SELECT setval(pg_get_serial_sequence('patient', 'id'), (SELECT MAX(id) FROM patient));
SELECT setval(pg_get_serial_sequence('appointment', 'id'), (SELECT MAX(id) FROM appointment));

//data from ai, alt+x in dbeaver to run 
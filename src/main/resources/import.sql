
DELETE FROM credentials WHERE username = 'admin';
DELETE FROM users WHERE email = 'admin@siwlibrary.com';


-- Autori
INSERT INTO author(name, surname, birth_date, death_date, nationality, photo_file_name) VALUES ('Alessandro', 'Manzoni', '1785-03-07', '1873-05-22', 'Italia', 'manzoni.jpg');
INSERT INTO author(name, surname, birth_date, death_date, nationality, photo_file_name) VALUES ('Dante', 'Alighieri', '1265-01-01', '1321-09-14', 'Italia', 'dante.jpg');
INSERT INTO author(name, surname, birth_date, death_date, nationality, photo_file_name) VALUES ('Umberto', 'Eco', '1932-01-05', '2016-02-19', 'Italia', 'eco.jpg');
INSERT INTO author(name, surname, birth_date, death_date, nationality, photo_file_name) VALUES ('Italo', 'Calvino', '1923-10-15', '1985-09-19', 'Italia', 'calvino.jpg');
INSERT INTO author(name, surname, birth_date, death_date, nationality, photo_file_name) VALUES ('Elena', 'Ferrante', '1943-04-01', null, 'Italia', 'ferrante.jpg');
INSERT INTO author(name, surname, birth_date, death_date, nationality, photo_file_name) VALUES ('Roberto', 'Saviano', '1979-09-22', null, 'Italia', 'saviano.jpg');
INSERT INTO author(name, surname, birth_date, death_date, nationality, photo_file_name) VALUES ('Primo', 'Levi', '1919-07-31', '1987-04-11', 'Italia', 'levi.jpg');
INSERT INTO author(name, surname, birth_date, death_date, nationality, photo_file_name) VALUES ('Niccolò', 'Ammaniti', '1966-09-25', null, 'Italia', 'ammaniti.jpg');
INSERT INTO author(name, surname, birth_date, death_date, nationality, photo_file_name) VALUES ('Andrea', 'Camilleri', '1925-09-06', '2019-07-17', 'Italia', 'camilleri.jpg');
INSERT INTO author(name, surname, birth_date, death_date, nationality, photo_file_name) VALUES ('Cesare', 'Pavese', '1908-09-09', '1950-08-27', 'Italia', 'pavese.jpg');
INSERT INTO author(name, surname, birth_date, death_date, nationality, photo_file_name) VALUES ('William', 'Shakespeare', '1564-04-23', '1616-04-23', 'Inghilterra', 'william.jpg');

-- Libri
INSERT INTO book(title, year, photo_file_name) VALUES ('I Promessi Sposi', 1827, 'IPromessiSposi.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('La Divina Commedia', 1320, 'LaDivinaCommedia.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('Il Nome della Rosa', 1980, 'IlNomeDellaRosa.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('Le Città Invisibili', 1972, 'LeCittaInvisibili.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('L''Amica Geniale', 2011, 'amicageniale.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('Storia del Nuovo Cognome', 2012, 'nuovocognome.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('Storia di Chi Fugge e di Chi Resta', 2013, 'chifugge.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('Gomorra', 2006, 'gomorra.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('Se Questo è un Uomo', 1947, 'sequesto.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('La Tregua', 1963, 'latregua.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('Io Non Ho Paura', 2001, 'iononhopaura.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('Il Commissario Montalbano', 1994, 'montalbano.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('La Luna e i Falò', 1950, 'lunaefalo.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('Paesi Tuoi', 1941, 'paesituoi.jpg');
INSERT INTO book(title, year, photo_file_name) VALUES ('Romeo e Giulietta', 1595, 'giulietta.jpg');

-- Relazioni libro-autore
INSERT INTO book_author(book_id, author_id) VALUES (1, 1);
INSERT INTO book_author(book_id, author_id) VALUES (2, 2);
INSERT INTO book_author(book_id, author_id) VALUES (3, 3);
INSERT INTO book_author(book_id, author_id) VALUES (4, 4);
INSERT INTO book_author(book_id, author_id) VALUES (5, 5);
INSERT INTO book_author(book_id, author_id) VALUES (6, 5);
INSERT INTO book_author(book_id, author_id) VALUES (7, 5);
INSERT INTO book_author(book_id, author_id) VALUES (8, 6);
INSERT INTO book_author(book_id, author_id) VALUES (9, 7);
INSERT INTO book_author(book_id, author_id) VALUES (10, 7);
INSERT INTO book_author(book_id, author_id) VALUES (11, 8);
INSERT INTO book_author(book_id, author_id) VALUES (12, 9);
INSERT INTO book_author(book_id, author_id) VALUES (13, 10);
INSERT INTO book_author(book_id, author_id) VALUES (14, 10);
INSERT INTO book_author(book_id, author_id) VALUES (15, 11);


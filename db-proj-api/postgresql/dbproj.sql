CREATE TABLE person (
	id	 SERIAL,
	name	 VARCHAR(50) NOT NULL,
	address	 CHAR(100) NOT NULL,
	phone	 VARCHAR(14) NOT NULL,
	username VARCHAR(50) NOT NULL,
	password VARCHAR(50) NOT NULL,
	PRIMARY KEY(id)
);

CREATE TABLE seller (
	business_name	 VARCHAR(50),
	rating		 FLOAT(1) NOT NULL,
	auctions_created INTEGER NOT NULL,
	person_id	 INTEGER,
	PRIMARY KEY(person_id)
);

CREATE TABLE bid (
	bid_id		 SERIAL,
	bid_amount	 FLOAT(2) NOT NULL,
	bid_time	 DATE NOT NULL,
	auction_aid	 INTEGER NOT NULL,
	auction_isbn	 INTEGER NOT NULL,
	buyer_person_id INTEGER NOT NULL,
	PRIMARY KEY(bid_id)
);

CREATE TABLE item (
	isbn		 INTEGER,
	title		 VARCHAR(30) NOT NULL,
	PRIMARY KEY(isbn)
);

CREATE TABLE auction (
	aid		 INTEGER,
	isCancelled BOOL,
	isbn		 INTEGER,
	start_date	 DATE,
	end_date	 DATE,
	current_bid	 INTEGER,
	description	 VARCHAR(1000),
	item_isbn	 INTEGER NOT NULL,
	seller_person_id INTEGER NOT NULL,
	PRIMARY KEY(aid,isbn)
);

CREATE TABLE buyer (
	bids_placed INTEGER,
	items_won	 INTEGER,
	person_id	 INTEGER,
	PRIMARY KEY(person_id)
);

ALTER TABLE seller ADD CONSTRAINT seller_fk1 FOREIGN KEY (person_id) REFERENCES person(id);
ALTER TABLE bid ADD CONSTRAINT bid_fk1 FOREIGN KEY (auction_aid, auction_isbn) REFERENCES auction(aid, isbn);
ALTER TABLE bid ADD CONSTRAINT bid_fk2 FOREIGN KEY (buyer_person_id) REFERENCES buyer(person_id);
ALTER TABLE auction ADD CONSTRAINT auction_fk1 FOREIGN KEY (item_isbn) REFERENCES item(isbn);
ALTER TABLE auction ADD CONSTRAINT auction_fk2 FOREIGN KEY (seller_person_id) REFERENCES seller(person_id);
ALTER TABLE buyer ADD CONSTRAINT buyer_fk1 FOREIGN KEY (person_id) REFERENCES person(id);

CREATE TABLE auction_history (
    history_id SERIAL PRIMARY KEY,
    operation_type VARCHAR(10) NOT NULL,
    aid INTEGER,
    isbn INTEGER,
    start_date DATE,
    end_date DATE,
    current_bid INTEGER,
    description VARCHAR(1000),
    item_isbn INTEGER,
    seller_person_id INTEGER
);

-- Functions for triggers
CREATE OR REPLACE FUNCTION log_auction_insert()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO auction_history (operation_type, aid, isbn, start_date, end_date, current_bid, description, item_isbn, seller_person_id)
    VALUES ('INSERT', NEW.aid, NEW.isbn, NEW.start_date, NEW.end_date, NEW.current_bid, NEW.description, NEW.item_isbn, NEW.seller_person_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION log_auction_update()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO auction_history (operation_type, aid, isbn, start_date, end_date, current_bid, description, item_isbn, seller_person_id)
    VALUES ('UPDATE', OLD.aid, OLD.isbn, OLD.start_date, OLD.end_date, OLD.current_bid, OLD.description, OLD.item_isbn, OLD.seller_person_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers
CREATE TRIGGER trigger_auction_insert
AFTER INSERT ON auction
FOR EACH ROW
EXECUTE FUNCTION log_auction_insert();

CREATE TRIGGER trigger_auction_update
AFTER UPDATE ON auction
FOR EACH ROW
EXECUTE FUNCTION log_auction_update();

INSERT INTO person (name, address, phone, username, password) VALUES
('John Doe', '123 Maple St', '555-1234', 'johndoe', 'pass1234'),
('Jane Smith', '456 Oak St', '555-5678', 'janesmith', 'pass5678');

INSERT INTO seller (business_name, rating, auctions_created, person_id) VALUES
('Johns Great Goods', 4.5, 10, 1),
('Janes Fine Finds', 4.7, 15, 2);

INSERT INTO person (name, address, phone, username, password) VALUES
('Alice Johnson', '789 Birch St', '555-6789', 'alicej', 'pass6789'),
('Bob Ray', '321 Pine St', '555-4321', 'bobr', 'pass4321');

INSERT INTO buyer (bids_placed, items_won, person_id) VALUES
(3, 1, 3),
(5, 2, 4);

INSERT INTO item (isbn, title) VALUES
(1001, 'Antique Vase'),
(1002, 'Vintage Watch');

INSERT INTO auction (aid, isbn, start_date, end_date, current_bid, description, item_isbn, seller_person_id) VALUES
(101, 1001, '2023-05-01', '2023-05-10', NULL, 'A beautiful antique vase from the 19th century.', 1001, 1),
(102, 1002, '2023-05-02', '2023-05-11', NULL, 'A vintage watch from the 1950s in excellent condition.', 1002, 2);
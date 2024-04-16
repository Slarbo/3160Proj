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

CREATE TABLE category (
	category_id SERIAL,
	PRIMARY KEY(category_id)
);

CREATE TABLE item (
	isbn		 INTEGER,
	item_status		 BOOL,
	title		 VARCHAR(30) NOT NULL,
	category_category_id INTEGER NOT NULL,
	PRIMARY KEY(isbn)
);

CREATE TABLE auction (
	aid		 INTEGER,
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
ALTER TABLE item ADD CONSTRAINT item_fk1 FOREIGN KEY (category_category_id) REFERENCES category(category_id);
ALTER TABLE auction ADD CONSTRAINT auction_fk1 FOREIGN KEY (item_isbn) REFERENCES item(isbn);
ALTER TABLE auction ADD CONSTRAINT auction_fk2 FOREIGN KEY (seller_person_id) REFERENCES seller(person_id);
ALTER TABLE buyer ADD CONSTRAINT buyer_fk1 FOREIGN KEY (person_id) REFERENCES person(id);

-- Replace this by the SQL code needed to create your database

CREATE TABLE person_inbox (
    id     SERIAL,
    name     VARCHAR(50) NOT NULL,
    address     CHAR(100) NOT NULL,
    phone     VARCHAR(14) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE seller (
    business_name     VARCHAR(50),
    rating   	  FLOAT(1) NOT NULL,
    auctions_created INTEGER NOT NULL,
    person_inbox_id     INTEGER,
    PRIMARY KEY(person_inbox_id)
);

CREATE TABLE bid_auction_comment_board_item (
    bid_id   					  SERIAL,
    bid_amount   				  FLOAT(2) NOT NULL,
    bid_time   					  DATE NOT NULL,
    auction_comment_board_item_aid   		  INTEGER NOT NULL,
    auction_comment_board_item_isbn   		  INTEGER NOT NULL,
    auction_comment_board_item_start_date   	  DATE,
    auction_comment_board_item_end_date   	  DATE,
    auction_comment_board_item_current_bid   	  INTEGER,
    auction_comment_board_item_description   	  VARCHAR(1000),
    auction_comment_board_item_comment_board_board_id BIGSERIAL NOT NULL,
    auction_comment_board_item_item_isbn   	  INTEGER NOT NULL,
    auction_comment_board_item_item_item_status     BOOL,
    auction_comment_board_item_item_title   	  VARCHAR(30) NOT NULL,
    category_category_id   			  INTEGER NOT NULL,
    seller_person_inbox_id   			  INTEGER NOT NULL,
    buyer_person_inbox_id   			  INTEGER NOT NULL,
    PRIMARY KEY(bid_id)
);

CREATE TABLE category (
    category_id SERIAL,
    PRIMARY KEY(category_id)
);

CREATE TABLE buyer (
    bids_placed     INTEGER,
    items_won     INTEGER,
    person_inbox_id INTEGER,
    PRIMARY KEY(person_inbox_id)
);

CREATE TABLE replies (
    reply_id   			  SERIAL,
    reply   			  VARCHAR(500) NOT NULL,
    time_posted   			  DATE NOT NULL,
    bid_auction_comment_board_item_bid_id INTEGER NOT NULL,
    questions_question_id   	  INTEGER NOT NULL,
    PRIMARY KEY(reply_id)
);

CREATE TABLE questions (
    question_id   			  SERIAL,
    question   			  VARCHAR(500) NOT NULL,
    time_posted   			  DATE NOT NULL,
    bid_auction_comment_board_item_bid_id INTEGER NOT NULL,
    PRIMARY KEY(question_id)
);

CREATE TABLE message (
    message_id   			  SERIAL,
    message   			  VARCHAR(1000),
    bid_auction_comment_board_item_bid_id     INTEGER NOT NULL,
    bid_auction_comment_board_item_bid_id1 INTEGER NOT NULL,
    PRIMARY KEY(message_id)
);

CREATE TABLE message_person_inbox (
    message_message_id INTEGER,
    person_inbox_id     INTEGER,
    PRIMARY KEY(message_message_id,person_inbox_id)
);

ALTER TABLE seller ADD CONSTRAINT seller_fk1 FOREIGN KEY (person_inbox_id) REFERENCES person_inbox(id);
ALTER TABLE bid_auction_comment_board_item ADD UNIQUE (auction_comment_board_item_aid, auction_comment_board_item_isbn, auction_comment_board_item_comment_board_board_id, auction_comment_board_item_item_isbn);
ALTER TABLE bid_auction_comment_board_item ADD CONSTRAINT bid_auction_comment_board_item_fk1 FOREIGN KEY (category_category_id) REFERENCES category(category_id);
ALTER TABLE bid_auction_comment_board_item ADD CONSTRAINT bid_auction_comment_board_item_fk2 FOREIGN KEY (seller_person_inbox_id) REFERENCES seller(person_inbox_id);
ALTER TABLE bid_auction_comment_board_item ADD CONSTRAINT bid_auction_comment_board_item_fk3 FOREIGN KEY (buyer_person_inbox_id) REFERENCES buyer(person_inbox_id);
ALTER TABLE buyer ADD CONSTRAINT buyer_fk1 FOREIGN KEY (person_inbox_id) REFERENCES person_inbox(id);
ALTER TABLE replies ADD CONSTRAINT replies_fk1 FOREIGN KEY (bid_auction_comment_board_item_bid_id) REFERENCES bid_auction_comment_board_item(bid_id);
ALTER TABLE replies ADD CONSTRAINT replies_fk2 FOREIGN KEY (questions_question_id) REFERENCES questions(question_id);
ALTER TABLE questions ADD CONSTRAINT questions_fk1 FOREIGN KEY (bid_auction_comment_board_item_bid_id) REFERENCES bid_auction_comment_board_item(bid_id);
ALTER TABLE message ADD CONSTRAINT message_fk1 FOREIGN KEY (bid_auction_comment_board_item_bid_id) REFERENCES bid_auction_comment_board_item(bid_id);
ALTER TABLE message ADD CONSTRAINT message_fk2 FOREIGN KEY (bid_auction_comment_board_item_bid_id1) REFERENCES bid_auction_comment_board_item(bid_id);
ALTER TABLE message_person_inbox ADD CONSTRAINT message_person_inbox_fk1 FOREIGN KEY (message_message_id) REFERENCES message(message_id);
ALTER TABLE message_person_inbox ADD CONSTRAINT message_person_inbox_fk2 FOREIGN KEY (person_inbox_id) REFERENCES person_inbox(id);


COMMIT;


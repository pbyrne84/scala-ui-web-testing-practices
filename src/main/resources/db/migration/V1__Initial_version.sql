CREATE SEQUENCE customer_id_seq;
CREATE SEQUENCE item_id_seq;

CREATE TABLE "user"
(
    id      VARCHAR(36) PRIMARY KEY,
    name    VARCHAR(200) NOT NULL,
    created TIMESTAMP    NOT NULL,
    updated TIMESTAMP    NOT NULL
);

CREATE TABLE item
(
    id                VARCHAR(36) PRIMARY KEY,
    user_id           VARCHAR(36) NOT NULL,
    title             VARCHAR(200)  NOT NULL,
    brief_description VARCHAR(400)  NOT NULL,
    description       VARCHAR(4000) NOT NULL,
    price             DECIMAL       NOT NULL,
    created           TIMESTAMP     NOT NULL,
    updated           TIMESTAMP     NOT NULL, /*could be nullable or we just put created value in at start*/
    FOREIGN KEY (user_id) REFERENCES "user" (id)
);

CREATE TABLE user_order
(
    id      VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NULL,
    created TIMESTAMP   NOT NULL
);

/*Could have a count field but we can have fun doing other stuff (aggregates)*/
CREATE TABLE user_order_item
(
    order_id VARCHAR(36) NOT NULL,
    item_id  VARCHAR(36) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES user_order (id),
    FOREIGN KEY (item_id) REFERENCES item (id)
);





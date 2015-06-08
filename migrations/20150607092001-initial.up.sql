-- migration to be applied

CREATE TABLE classified_site (
    id serial NOT NULL PRIMARY KEY,
    country_code text NOT NULL,
    city text,
    url text NOT NULL,
    description text NOT NULL DEFAULT '',
    published boolean DEFAULT false
);

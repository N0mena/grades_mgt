alter table "user"
    add column if not exists promotion_id uuid references promotion (id);

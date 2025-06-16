create materialized view vw_meet as
select distinct
    federation,
    date,
    meet_country,
    meet_state,
    meet_town,
    meet_name
from event;

create index if not exists idx_meet_federation
on vw_meet (federation);

create index if not exists idx_meet_name
on vw_meet (meet_name);

create index if not exists idx_meet_country
on vw_meet (meet_country);

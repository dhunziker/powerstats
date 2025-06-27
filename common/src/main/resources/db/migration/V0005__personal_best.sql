create materialized view if not exists vw_personal_best as
select
    name,
    sex,
    equipment,
    max(best_3_squat_kg) as best_3_squat_kg,
    max(best_3_bench_kg) as best_3_bench_kg,
    max(best_3_deadlift_kg) as best_3_deadlift_kg,
    max(total_kg) as total_kg,
    max(dots) as dots,
    max(wilks) as wilks,
    max(glossbrenner) as glossbrenner,
    max(goodlift) as goodlift
from event
group by name, sex, equipment;

create extension if not exists pg_trgm;

create index if not exists idx_personal_best_name_trgm
on vw_personal_best using gin (name gin_trgm_ops);

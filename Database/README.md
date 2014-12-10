# Database

## Number of private comments per day

`SELECT FROM_UNIXTIME(time_inserted, '%Y-%m-%d') AS date_str, COUNT(*) AS private_messages FROM comments WHERE private_to_user IS NOT NULL GROUP BY date_str`

## Number of public comments per day

`SELECT FROM_UNIXTIME(time_inserted, '%Y-%m-%d') AS date_str, COUNT(*) AS public_messages FROM comments WHERE private_to_user IS NULL GROUP BY date_str`

## Number of messages by language (past 7 days)

`SELECT language_iso3 AS language_str, COUNT(*) AS messages_published FROM messages WHERE time_published > (UNIX_TIMESTAMP()-3600*24*7) GROUP BY language_str ORDER BY messages_published DESC`

## Number of messages by country (past 7 days)

`SELECT country_iso3 AS country_str, COUNT(*) AS messages_published FROM messages WHERE time_published > (UNIX_TIMESTAMP()-3600*24*7) GROUP BY country_str ORDER BY messages_published DESC`

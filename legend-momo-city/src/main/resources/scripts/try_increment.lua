local current = redis.call('GET', KEYS[1])

if not current then
    current = 0
else
    current = tonumber(current)
end

if current < tonumber(ARGV[1]) then
    return redis.call('INCR', KEYS[1])
else
    return -1
end
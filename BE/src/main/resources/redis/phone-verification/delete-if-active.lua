if redis.call('GET', KEYS[2]) == ARGV[1] then
    redis.call('DEL', KEYS[1])
    redis.call('DEL', KEYS[2])
    redis.call('DEL', KEYS[3])
    return 1
end
return 0

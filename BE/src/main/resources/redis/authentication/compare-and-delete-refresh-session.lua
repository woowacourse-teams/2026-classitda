local sessionValue = redis.call('GET', KEYS[1])
if not sessionValue then
    return 1
end

local decoded, session = pcall(cjson.decode, sessionValue)
if not decoded or type(session) ~= 'table' then
    return -1
end

if type(session.tokenHash) ~= 'string'
        or string.len(session.tokenHash) ~= 64
        or not string.match(session.tokenHash, '^[0-9a-f]+$')
        or type(session.memberId) ~= 'number'
        or session.memberId < 1
        or session.memberId ~= math.floor(session.memberId)
        or type(session.expiresAtEpochSecond) ~= 'number'
        or session.expiresAtEpochSecond < 1
        or session.expiresAtEpochSecond ~= math.floor(session.expiresAtEpochSecond) then
    return -1
end

if sessionValue ~= ARGV[1] then
    return 1
end

local deleted = redis.call('DEL', KEYS[1])
if deleted ~= 1 then
    return -1
end

return 0

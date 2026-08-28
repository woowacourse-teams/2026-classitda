local oldSessionValue = redis.call('GET', KEYS[1])
if not oldSessionValue then
    return 1
end

local decoded, oldSession = pcall(cjson.decode, oldSessionValue)
if not decoded or type(oldSession) ~= 'table' then
    return -1
end

local expectedOldSessionValue = ARGV[1]
local newSessionValue = ARGV[2]
local nowEpochSecond = tonumber(ARGV[3])
local newSessionTtl = tonumber(ARGV[4])

if type(oldSession.tokenHash) ~= 'string'
        or type(oldSession.memberId) ~= 'number'
        or oldSession.memberId < 1
        or oldSession.memberId ~= math.floor(oldSession.memberId)
        or type(oldSession.expiresAtEpochSecond) ~= 'number'
        or oldSession.expiresAtEpochSecond < 1
        or oldSession.expiresAtEpochSecond ~= math.floor(oldSession.expiresAtEpochSecond)
        or not nowEpochSecond
        or nowEpochSecond < 1
        or nowEpochSecond ~= math.floor(nowEpochSecond)
        or not newSessionTtl
        or newSessionTtl < 1
        or newSessionTtl ~= math.floor(newSessionTtl) then
    return -1
end

if oldSessionValue ~= expectedOldSessionValue
        or nowEpochSecond >= oldSession.expiresAtEpochSecond then
    return 1
end

if redis.call('EXISTS', KEYS[2]) == 1 then
    return 2
end

local saved = redis.call('SET', KEYS[2], newSessionValue, 'EX', newSessionTtl, 'NX')
if not saved then
    return 2
end

redis.call('DEL', KEYS[1])
return 0

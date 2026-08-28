local verificationId = ARGV[1]
local signupJti = ARGV[2]
local expectedOtpDigest = ARGV[3]
local otpMatches = ARGV[4]
local maxAttempts = tonumber(ARGV[5])
local verifiedPhoneTtl = tonumber(ARGV[6])

if redis.call('EXISTS', KEYS[1]) == 0 then
    return 1
end

local storedSignupJti = redis.call('HGET', KEYS[1], 'signupJti')
local storedPhoneNumber = redis.call('HGET', KEYS[1], 'phoneNumber')
local storedOtpDigest = redis.call('HGET', KEYS[1], 'otpDigest')
if not storedSignupJti or not storedPhoneNumber or not storedOtpDigest then
    return -1
end

if storedSignupJti ~= signupJti then
    return 2
end

if storedOtpDigest ~= expectedOtpDigest then
    return -1
end

if redis.call('GET', KEYS[2]) ~= verificationId then
    return 1
end

if not maxAttempts or maxAttempts < 1 or not verifiedPhoneTtl or verifiedPhoneTtl < 1 then
    return -1
end

local failedAttemptsValue = redis.call('HGET', KEYS[1], 'failedAttempts')
local failedAttempts = 0
if failedAttemptsValue then
    failedAttempts = tonumber(failedAttemptsValue)
    if not failedAttempts or failedAttempts < 0 or failedAttempts ~= math.floor(failedAttempts) then
        return -1
    end
end

if redis.call('EXISTS', KEYS[3]) == 1 then
    redis.call('DEL', KEYS[1])
    redis.call('DEL', KEYS[2])
    return 1
end

if failedAttempts >= maxAttempts then
    return 4
end

if otpMatches == '0' then
    failedAttempts = redis.call('HINCRBY', KEYS[1], 'failedAttempts', 1)
    if failedAttempts >= maxAttempts then
        return 4
    end
    return 3
end

if otpMatches ~= '1' then
    return -1
end

local verified = redis.call('SET', KEYS[3], storedPhoneNumber, 'EX', verifiedPhoneTtl, 'NX')
redis.call('DEL', KEYS[1])
redis.call('DEL', KEYS[2])
if not verified then
    return 1
end
return 0

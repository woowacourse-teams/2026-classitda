local verificationId = ARGV[1]
local signupJti = ARGV[2]
local phoneNumber = ARGV[3]
local otpDigest = ARGV[4]
local verificationTtl = ARGV[5]
local cooldownTtl = ARGV[6]
local verificationKeyPrefix = ARGV[7]

if redis.call('EXISTS', KEYS[3]) == 1 then
    return 1
end

local previousVerificationId = redis.call('GET', KEYS[2])
if previousVerificationId and previousVerificationId ~= verificationId then
    redis.call('DEL', verificationKeyPrefix .. previousVerificationId)
end

redis.call('HSET', KEYS[1],
    'signupJti', signupJti,
    'phoneNumber', phoneNumber,
    'otpDigest', otpDigest)
redis.call('EXPIRE', KEYS[1], verificationTtl)
redis.call('SET', KEYS[2], verificationId, 'EX', verificationTtl)
redis.call('SET', KEYS[3], '1', 'EX', cooldownTtl)
return 0

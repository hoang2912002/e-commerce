local raw_data = redis.call('GET', KEYS[1])

if not raw_data then
    return - 2
end

local typed_data = cjson.decode(raw_data)
local data = typed_data[2]
local request_quantity = tonumber(ARGV[1])

if data.quantity then
    data.quantity = data.quantity + request_quantity
else
    data.quantity = request_quantity
end

typed_data[2] = data
local updated_json = cjson.encode(typed_data)

redis.call('set', KEYS[1], updated_json)

return data.quantity



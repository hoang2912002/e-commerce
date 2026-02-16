local raw_data = redis.call('GET', KEYS[1])

if not raw_data then
    return - 2
end

local typed_data = cjson.decode(raw_data)
local data = typed_data[2]
local request_stock = tonumber(ARGV[1])

if data.stock then
    data.stock = data.stock + request_stock
else
    data.stock = request_stock
end

typed_data[2] = data
local updated_json = cjson.encode(typed_data)

redis.call('set', KEYS[1], updated_json)

return data.stock



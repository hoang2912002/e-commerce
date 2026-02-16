-- =====================================================================
-- KEYS[1]: Key của item
-- ARGV[1]: Số lượng cần hoàn lại
-- =====================================================================

local raw_data = redis.call('GET', KEYS[1])
if not raw_data then return -2 end

local typed_data = cjson.decode(raw_data)
local data = typed_data[2]
local amount = tonumber(ARGV[1])

local available = tonumber(data.quantityAvailable or 0)
local reserved = tonumber(data.quantityReserved or 0)

if reserved < amount then
    amount = reserved -- Chỉ trả lại tối đa những gì đang giữ
end

data.quantityAvailable = available + amount
data.quantityReserved = reserved - amount

typed_data[2] = data
redis.call('SET', KEYS[1], cjson.encode(typed_data))

return data.quantityAvailable
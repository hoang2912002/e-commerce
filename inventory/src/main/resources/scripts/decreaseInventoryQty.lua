-- =====================================================================
-- KEYS[1]: Key của item (ví dụ: fsh:ord:couI:...)
-- ARGV[1]: Số lượng cần xí chỗ
-- =====================================================================

local raw_data = redis.call('GET', KEYS[1])
if not raw_data then return -2 end

local typed_data = cjson.decode(raw_data)
local data = typed_data[2]
local amount = tonumber(ARGV[1])

-- Kiểm tra quantityAvailable
local available = tonumber(data.quantityAvailable or 0)
local reserved = tonumber(data.quantityReserved or 0)

if available < amount then
    return -1 -- Không đủ hàng có sẵn
end

data.quantityAvailable = available - amount
data.quantityReserved = reserved + amount

typed_data[2] = data
redis.call('SET', KEYS[1], cjson.encode(typed_data))

return data.quantityAvailable -- Trả về số lượng còn lại để update local cache
-- =====================================================================
-- KEYS[1]: Key của coupon
-- ARGV[1]: Số lượng cần trừ
-- =====================================================================

-- 1. Lấy dữ liệu thô từ Redis
local raw_data = redis.call('GET', KEYS[1])

-- 2. Kiểm tra nếu key không tồn tại
if not raw_data then
    return - 2
end

-- 3. Parse chuỗi JSON thành Table (Lua table)
local typed_data = cjson.decode(raw_data)

-- 4. Lấy Object chứa data thực tế
-- Trong chuỗi: ["className", {data}]
-- Index trong Lua bắt đầu từ 1, nên data nằm ở index 2
local data = typed_data[2]
local request_stock = tonumber(ARGV[1])

if not data.stock or tonumber(data.stock) < request_stock then
    return -1 -- Mã lỗi: Không đủ số lượng để trừ
end

data.stock = data.stock - request_stock

typed_data[2] = data

-- 5. Encode ngược lại thành chuỗi JSON
local updated_json = cjson.encode(typed_data)

-- 6. Ghi đè lại vào Redis
redis.call('SET', KEYS[1], updated_json)

return data.stock
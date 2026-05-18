-- wrk script: per-thread monotonic userId to avoid duplicate purchase rejection
wrk.method = "POST"
wrk.headers["Content-Type"] = "application/json"

local thread_counter = 0

setup = function(thread)
  thread:set("tid", thread_counter)
  thread_counter = thread_counter + 1
end

init = function(args)
  tid = wrk.thread:get("tid")
  counter = tid * 100000000
end

request = function()
  counter = counter + 1
  local body = string.format('{"userId":%d,"productId":1001,"quantity":1}', counter)
  return wrk.format(nil, nil, nil, body)
end

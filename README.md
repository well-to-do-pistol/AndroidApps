ChatGPT:

零:

> 1\. trim消除空白字符

1.  MainActivity

```{=html}

```

1. 把MessageList(通过不断往列表add实现历史记录)传给adapter,
   message由sendBy和message字符串组成; 根据sendBy判断是左边还是右边

2. 用runOnUiThread启动主线程来改变视图(adapter.notifyDataSetChanged),
   用recyclerView.smoothScrollToPosition移到adapter的终点

3. 用okhttp库:

   a.  设置OkHttpClient

   b.  把MediaType和标准JsonObject请求模板toString放进RequestBody,

   c.  Build request, 将request放进client的newCall

   d.  根据newCall的callback(失败则传失败字符串,
       成功则根据模板用JsonObject解析body(用.string()不用.toString()))

4. callAPI的时候先把一个等待消息添加进列表,
   在添加失败或成功消息时.remove(list.size()-1)移除等待消息

```{=html}

```

2.  请求问题

```{=html}

```

1.  Client的文本超时设为3分钟, 设上限为2000token

2.  点击sendButton的时候如果输入为空自动添加输入(有bug)

3.  先用一个newCall请求accessToken, 成功后再拿accessToken请求问答,
    成功后解析body添加消息

4.  client.newCall(request).enqueue是后台的网络请求

package com.github.musicode.xingepush.receiver

import android.content.Context
import android.content.Intent
import android.util.Log

import com.github.musicode.xingepush.Constant
import com.github.musicode.xingepush.RNTXingePushModule
import com.tencent.android.tpush.XGPushBaseReceiver
import com.tencent.android.tpush.XGPushClickedResult
import com.tencent.android.tpush.XGPushRegisterResult
import com.tencent.android.tpush.XGPushShowedResult
import com.tencent.android.tpush.XGPushTextMessage

// 信鸽的第三方推送经过它自己的封装后，会转发出以下两个 Intent，因此所有的推送在这个文件统一处理就行了
// com.tencent.android.tpush.action.PUSH_MESSAGE
// com.tencent.android.tpush.action.FEEDBACK

class MessageReceiver : XGPushBaseReceiver() {

    override fun onRegisterResult(context: Context, code: Int, result: XGPushRegisterResult) {

    }

    override fun onUnregisterResult(context: Context, code: Int) {

    }

    override fun onSetTagResult(context: Context?, code: Int, tagName: String) {

        if (context == null) {
            return
        }

        Log.d("XINGE", "[XINGE] onSetTagResult $code $tagName")

        val intent = Intent(Constant.ACTION_BIND_TAGS)
        intent.putExtra("code", code)
        context.sendBroadcast(intent)

    }

    override fun onDeleteTagResult(context: Context?, code: Int, tagName: String) {

        if (context == null) {
            return
        }

        Log.d("XINGE", "[XINGE] onDeleteTagResult $code $tagName")

        val intent = Intent(Constant.ACTION_UNBIND_TAGS)
        intent.putExtra("code", code)
        context.sendBroadcast(intent)

    }

    override fun onTextMessage(context: Context?, message: XGPushTextMessage) {

        if (context == null) {
            return
        }

        Log.d("XINGE", "[XINGE] onTextMessage $message")

        // ios 只能取到 custom content
        // 因此安卓只取 custom content

        val intent = Intent(Constant.ACTION_MESSAGE)

        var customContent: String? = message.customContent
        if (customContent == null || customContent.isEmpty()) {
            // 某些第三方厂商会忽略自定义参数，因此用 content 做降级
            val content = message.content
            // 如果 content 是个 json，就用他代替 customContent
            if (content != null && content.startsWith("{") && content.endsWith("}")) {
                customContent = content

                // 华为会多加一层 content
                val prefix = "{\"content\":\""
                val suffix = "\"}"

                if (customContent.startsWith(prefix) && customContent.endsWith(suffix)) {
                    customContent = customContent.substring(prefix.length, customContent.length - suffix.length)
                }

                customContent = customContent.replace("\\\\\"".toRegex(), "\"")
            }
        }

        intent.putExtra("customContent", customContent ?: "")

        context.sendBroadcast(intent)

    }

    override fun onNotifactionClickedResult(context: Context?, result: XGPushClickedResult) {

        if (context == null) {
            return
        }

        Log.d("XINGE", "[XINGE] onNotifactionClickedResult $result")

        val intent = Intent(Constant.ACTION_NOTIFICATION)
        val title = result.title
        val content = result.content
        val customContent = result.customContent

        intent.putExtra("title", title ?: "")
        intent.putExtra("content", content ?: "")
        intent.putExtra("customContent", customContent ?: "")

        val actionType = result.actionType
        if (actionType == XGPushClickedResult.NOTIFACTION_CLICKED_TYPE.toLong()) {
            intent.putExtra("clicked", true)
        }
        if (actionType == XGPushClickedResult.NOTIFACTION_DELETED_TYPE.toLong()) {
            intent.putExtra("deleted", true)
        }

        context.sendBroadcast(intent)

        // 在 RNTXingePushModule 还没初始化时，这个方法就会执行
        // 因此为了获取到启动 app 的那条推送，这里需要存一下
        if (!RNTXingePushModule.isStarted) {
            RNTXingePushModule.launchIntent = intent
        }

    }

    override fun onNotifactionShowedResult(context: Context?, result: XGPushShowedResult) {

        if (context == null) {
            return
        }

        Log.d("XINGE", "[XINGE] onNotifactionShowedResult $result")

        val intent = Intent(Constant.ACTION_NOTIFICATION)
        val title = result.title
        val content = result.content
        val customContent = result.customContent

        intent.putExtra("title", title ?: "")
        intent.putExtra("content", content ?: "")
        intent.putExtra("customContent", customContent ?: "")

        intent.putExtra("presented", true)

        context.sendBroadcast(intent)

    }
}

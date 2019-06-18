package com.github.musicode.xingepush.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.musicode.xingepush.Constant;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

// 信鸽的第三方推送经过它自己的封装后，会转发出以下两个 Intent，因此所有的推送在这个文件统一处理就行了
// com.tencent.android.tpush.action.PUSH_MESSAGE
// com.tencent.android.tpush.action.FEEDBACK

public class MessageReceiver extends XGPushBaseReceiver {

    @Override
    public void onRegisterResult(Context context, int code, XGPushRegisterResult result) {

    }

    @Override
    public void onUnregisterResult(Context context, int code) {

    }

    @Override
    public void onSetTagResult(Context context, int code, String tagName) {

        if (context == null) {
            return;
        }

        Log.d("XINGE", "[XINGE] onSetTagResult " + code + " " + tagName);

        Intent intent = new Intent(Constant.ACTION_BIND_TAGS);
        intent.putExtra("code", code);
        context.sendBroadcast(intent);

    }

    @Override
    public void onDeleteTagResult(Context context, int code, String tagName) {

        if (context == null) {
            return;
        }

        Log.d("XINGE", "[XINGE] onDeleteTagResult " + code + " " + tagName);

        Intent intent = new Intent(Constant.ACTION_UNBIND_TAGS);
        intent.putExtra("code", code);
        context.sendBroadcast(intent);

    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {

        if (context == null) {
            return;
        }

        Log.d("XINGE", "[XINGE] onTextMessage " + message.toString());

        // ios 只能取到 custom content
        // 因此安卓只取 custom content

        Intent intent = new Intent(Constant.ACTION_MESSAGE);

        String customContent = message.getCustomContent();
        if (customContent == null || customContent.isEmpty()) {
            // 某些第三方厂商会忽略自定义参数，因此用 content 做降级
            String content = message.getContent();
            // 如果 content 是个 json，就用他代替 customContent
            if (content != null && content.startsWith("{") && content.endsWith("}")) {
                customContent = content;

                // 华为会多加一层 content
                String prefix = "{\"content\":\"";
                String suffix = "\"}";

                if (customContent.startsWith(prefix)
                    && customContent.endsWith(suffix)
                ) {
                    customContent = customContent.substring(prefix.length(), customContent.length() - suffix.length());
                }

                customContent = customContent.replaceAll("\\\\\"", "\"");
            }
        }

        intent.putExtra("customContent", customContent == null ? "" : customContent);

        context.sendBroadcast(intent);

    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult result) {

        if (context == null) {
            return;
        }

        Log.d("XINGE", "[XINGE] onNotifactionClickedResult " + result.toString());

        Intent intent = new Intent(Constant.ACTION_NOTIFICATION);
        String title = result.getTitle();
        String content = result.getContent();
        String customContent = result.getCustomContent();

        intent.putExtra("title", title == null ? "" : title);
        intent.putExtra("content", content == null ? "" : content);
        intent.putExtra("customContent", customContent == null ? "" : customContent);

        long actionType = result.getActionType();
        if (actionType == XGPushClickedResult.NOTIFACTION_CLICKED_TYPE) {
            intent.putExtra("clicked", true);
        }
        if (actionType == XGPushClickedResult.NOTIFACTION_DELETED_TYPE) {
            intent.putExtra("deleted", true);
        }

        context.sendBroadcast(intent);

    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult result) {

        if (context == null) {
            return;
        }

        Log.d("XINGE", "[XINGE] onNotifactionShowedResult " + result.toString());

        Intent intent = new Intent(Constant.ACTION_NOTIFICATION);
        String title = result.getTitle();
        String content = result.getContent();
        String customContent = result.getCustomContent();

        intent.putExtra("title", title == null ? "" : title);
        intent.putExtra("content", content == null ? "" : content);
        intent.putExtra("customContent", customContent == null ? "" : customContent);

        intent.putExtra("presented", true);

        context.sendBroadcast(intent);

    }
}

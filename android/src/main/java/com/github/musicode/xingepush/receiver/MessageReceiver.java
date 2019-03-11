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

import org.json.JSONException;
import org.json.JSONObject;

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

        Intent intent = new Intent(Constant.EVENT_BIND_TAG);
        intent.putExtra("code", code);
        context.sendBroadcast(intent);

    }

    @Override
    public void onDeleteTagResult(Context context, int code, String tagName) {

        if (context == null) {
            return;
        }

        Intent intent = new Intent(Constant.EVENT_UNBIND_TAG);
        intent.putExtra("code", code);
        context.sendBroadcast(intent);

    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {

        if (context == null) {
            return;
        }

        Log.d("XINGE", "[XINGE] onTextMessage " + message.toString());

        Intent intent = new Intent(Constant.EVENT_MESSAGE);
        String title = message.getTitle();
        String content = message.getContent();
        String customContent = message.getCustomContent();

        // 华为透传消息，会多一层 content
        if (content != null && content.startsWith("{") && content.endsWith("}")) {
            try {
                JSONObject json = new JSONObject(content);
                if (json.length() == 1 && json.has("content")) {
                    content = json.getString("content");
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        intent.putExtra("title", title == null ? "" : title);
        intent.putExtra("content", content == null ? "" : content);
        intent.putExtra("customContent", customContent == null ? "" : customContent);

        context.sendBroadcast(intent);

    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult result) {

        if (context == null) {
            return;
        }

        Log.d("XINGE", "[XINGE] onNotifactionClickedResult " + result.toString());

        Intent intent = new Intent(Constant.EVENT_NOTIFICATION);
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

        Intent intent = new Intent(Constant.EVENT_NOTIFICATION);
        String title = result.getTitle();
        String content = result.getContent();
        String customContent = result.getCustomContent();

        intent.putExtra("title", title == null ? "" : title);
        intent.putExtra("content", content == null ? "" : content);
        intent.putExtra("customContent", customContent == null ? "" : customContent);

        intent.putExtra("showed", true);

        context.sendBroadcast(intent);

    }
}

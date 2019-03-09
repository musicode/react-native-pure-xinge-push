
import { NativeEventEmitter, NativeModules } from 'react-native'

const { RNTXingePush } = NativeModules

const eventEmitter = new NativeEventEmitter(RNTXingePush)

export default {

  start(accessId, accessKey) {
    if (typeof accessId !== 'number') {
      console.error(`[XingePush start] accessId is not a number.`)
    }
    if (typeof accessKey !== 'string') {
      console.error(`[XingePush start] accessKey is not a string.`)
    }
    RNTXingePush.start(accessId, accessKey)
  },

  stop() {
    RNTXingePush.stop()
  },

  bindAccount(account) {
    RNTXingePush.bindAccount(account)
  },

  unbindAccount(account) {
    RNTXingePush.unbindAccount(account)
  },

  bindTag(tag) {
    RNTXingePush.bindTag(tag)
  },

  unbindTag(tag) {
    RNTXingePush.unbindTag(tag)
  },

  reportLocation(latitude, longitude) {
    RNTXingePush.reportLocation(latitude, longitude)
  },

  setBadge(badgeNumber) {
    RNTXingePush.setBadge(badgeNumber)
  },

  setDebug(enable) {
    RNTXingePush.setDebug(enable)
  },

  addListener(name, listener) {
    return eventEmitter.addListener(name, listener)
  }

}

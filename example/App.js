/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 * @lint-ignore-every XPLATJSCOPYRIGHT1
 */

import React, {Component} from 'react';
import {Platform, StyleSheet, Text, View, Alert} from 'react-native';

import XingePush from 'react-native-pure-xinge-push'
console.log(XingePush)
function alert(name, data) {
  console.log(name, data)
  // Alert.alert(
  //   name,
  //   JSON.stringify(data)
  // )
}

XingePush.addEventListener('start', function (data) {
  alert('start', data)
})

XingePush.addEventListener('stop', function (data) {
  alert('stop', data)
})

XingePush.addEventListener('bindAccount', function (data) {
  alert('bindAccount', data)
})

XingePush.addEventListener('unbindAccount', function (data) {
  alert('unbindAccount', data)
})

XingePush.addEventListener('bindTags', function (data) {
  alert('bindTag', data)
})

XingePush.addEventListener('unbindTags', function (data) {
  alert('unbindTag', data)
})

XingePush.addEventListener('register', function (data) {
  alert('resgiter', data)
})

XingePush.addEventListener('message', function (data) {
  alert('message', data)
})
XingePush.addEventListener('notification', function (data) {
  alert('notification', data)
})

const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
  android:
    'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});

type Props = {};
export default class App extends Component<Props> {
  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome} onPress={() => {
          XingePush.setDebug(true)
        }}>
          setDebug(true)
        </Text>

        <Text style={styles.welcome} onPress={() => {
          XingePush.enableOtherPush(true)
        }}>
          enableOtherPush(true)
        </Text>

        <Text style={styles.welcome} onPress={() => {
          XingePush.setHuaweiDebug(true)
        }}>
          setHuaweiDebug(true)
        </Text>

        <Text style={styles.welcome} onPress={() => {
          XingePush.start(2200278495, 'IE8K3754YFRS')
          // XingePush.start(2100279083, 'AX2NPJ35J13C')
        }}>
          start
        </Text>
        <Text style={styles.welcome} onPress={() => {
          XingePush.stop()
        }}>
          stop
        </Text>
        <Text style={styles.welcome} onPress={() => {
          XingePush.bindAccount('tester')
        }}>
          bindAccount
        </Text>
        <Text style={styles.welcome} onPress={() => {
          XingePush.unbindAccount('tester')
        }}>
          unbindAccount
        </Text>

        <Text style={styles.welcome} onPress={() => {
          XingePush.bindTag('tester')
        }}>
          bindTag
        </Text>
        <Text style={styles.welcome} onPress={() => {
          XingePush.unbindTag('tester')
        }}>
          unbindTag
        </Text>

        <Text style={styles.welcome} onPress={() => {
          XingePush.setBadge(0)
        }}>
          setBadge(0)
        </Text>
        <Text style={styles.welcome} onPress={() => {
          XingePush.setBadge(10)
        }}>
          setBadge(10)
        </Text>
        <Text style={styles.welcome} onPress={() => {
          XingePush.getBadge().then(data => {
            alert('getBadge', data)
          })
        }}>
          getBadge()
        </Text>

      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

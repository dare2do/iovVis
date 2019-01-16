/*
 * Copyright © 2016-2018 The ET-iLink Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* eslint-disable import/no-commonjs */
/* eslint-disable global-require */
/* eslint-disable import/no-nodejs-modules */

// const path = require('path');
const axios = require('axios');

function postCrackWidth(point) {
    axios.post('http://cf.beidouapp.com:8080/api/v1/RPY9wgslUsoUEIoDADrc/telemetry', //devA 
        point).then(res => {
            console.log(res.status);
        }).catch(e => {
            // console.info(e);
        })
}

function postCrackDeepth(ptJson) {
    axios.post('http://cf.beidouapp.com:8080/api/v1/ieE9pWSpcX7fKkj9G9q0/telemetry', //devB 
        ptJson).then(res => {
            console.info(res.status);
        }).catch(e => {
            // console.info(e);
        })
}

function postCliAttrValue(ptJson) {
    axios.post('http://cf.beidouapp.com:8080/api/v1/ieE9pWSpcX7fKkj9G9q0/attributes', //devB               
        ptJson).then(res => {
            console.info(res.status);
        }).catch(e => {
            // console.info(e);
        })
}

let pt = { "crackWidth": 82, idType: "CRACK-ID", alarmCnt: 10.79 };
postCrackWidth(pt);

setInterval(postCrackWidth, 5000, pt);

// let pt2 = { "裂缝深度": 0.4, idType: "CRACK", alarmCnt: 1 };
// postCrackDeepth(pt2);

// let attr = {"AMonitorPosX":1233.3555};
// postCliAttrValue(attr);

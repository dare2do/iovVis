const axios = require('axios');
const node_echarts = require('node-echarts');
const util  = require('../../../util/utils');

function SendPngResponse(option, params, res){
    if (!option)
    {
        util.responData(404, '访问资源不存在。', res);
        return;  
    }

    let config = {
        width: params.chartWidth   ? params.chartWidth  * 100 : 500, // Image width, type is number.
        height: params.chartHeight ? params.chartHeight * 100 : 400, // Image height, type is number.
        option: option, // Echarts configuration, type is Object.
        //If the path  is not set, return the Buffer of image.
        // path:  '', // Path is filepath of the image which will be created.
        enableAutoDispose: true  //Enable auto-dispose echarts after the image is created.
    }
    let bytes = node_echarts(config);
    if (bytes) {
        let data = 'data:image/png;base64,' + bytes.toString('base64');
        util.responData(200, data, res);
    }
}

function processData(option, params, allData, maxCnt, res){
    // 检查数据有效性
    let dataValid = false;
    let chooseIdx = 0;
    let dataLen = 0;
    for (let i = 0; i < allData.length; i++){
        dataLen = allData[i].length;
        if (dataLen > 0) {
            dataValid = true;
            chooseIdx = i;
            break;
        }
    }

    if (dataValid){
        for (let i = 0; i < dataLen; i++) {                    
            for (let idx = 0; idx < maxCnt; idx++) {
                if (allData[idx] && allData[idx][i]) {
                    val = Number.parseFloat(allData[idx][i].value);
                    option.series[idx].data.push(val);
                }
            }

            // 天
            if (Number.parseInt(params.interval) == 86400) {
                var dat = new Date(allData[chooseIdx][i].ts);
                dat = util.dateFormat(dat,'yyyyMMdd');
                option.xAxis[0].data.push(dat);
            } else if (Number.parseInt(params.interval) == 86400/2) {
                var dat = new Date(allData[chooseIdx][i].ts);
                dat = util.dateFormat(dat,'MMddhhmm');
                option.xAxis[0].data.push(dat);
            }  else {
                var dat = new Date(allData[chooseIdx][i].ts);
                dat = util.dateFormat(dat,'hhmmss');
                option.xAxis[0].data.push(dat);
            }          
        }
    }

    SendPngResponse(option, params, res);
}

function getData(plotCfg, option, params, token, res){    
    if (plotCfg)
    {
        let diff = params.endTime - params.startTime;
        let interval = Number.parseFloat(params.interval) * 1000;

        let keyValue = plotCfg.keys;
        let api = util.getAPI() + `plugins/telemetry/DEVICE/${params.devid}/values/timeseries?keys=${keyValue}`
        + `&startTs=${params.startTime}&endTs=${params.endTime}&interval=${interval}&agg=AVG`;
        api = encodeURI(api);
        //console.log(api);

        axios.get(api, {
            headers: { "X-Authorization": token }
        }).then(response => {
            //console.log('idx:' + idx + ' return:' + retCnt);
            let keys = plotCfg.keys.split(',');
            let allData = [];
            for (let i = 0; i < plotCfg.maxCnt; i++){
                allData[i] = (response.data[keys[i]]) ? response.data[keys[i]] : [];
            }

            processData(option, params, allData, plotCfg.maxCnt, res);
        }).catch(err => {
            console.log("err" + err);
            processData(option, res, params, plotCfg.maxCnt, res);
        });   
    }
}

function resetPreData(option, maxCnt){
    option.xAxis[0].data = [];
    for (let idx = 0; idx < maxCnt; idx++) {
        option.series[idx].data = [];
    }
}

exports.resetPreData = resetPreData;
exports.getData = getData;
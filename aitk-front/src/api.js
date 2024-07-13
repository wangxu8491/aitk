import axios from "axios";
import {Message, Notification} from "element-ui";

export function getModelTreeData(param, successCallback) {
  return axios.post("/aitk/getModelTreeData", param).then(
      response => handleBizError(response, successCallback)).catch(
      handleError);
}

export function getSelf(param, successCallback) {
  return axios.post("/im/getSelf", param).then(
      response => handleBizError(response, successCallback)).catch(
      handleError);
}

export function handleMessage(param, successCallback) {
  return axios.post("/im/handleMessage", param).then(
      response => handleBizError(response, successCallback)).catch(
      handleError);
}

export function pullMessage(param, successCallback) {
  return axios.get("/im/pullMessage", {params: param}).then(
      response => handleBizError(response, successCallback)).catch(handleError);
}

function handleBizError(response, successCallback) {
  successCallback(response.data);
}

function handleError(error) {
  console.log(error)
  Notification.error({
    title: '服务端错误',
    message: error,
    type: 'error'
  })
}

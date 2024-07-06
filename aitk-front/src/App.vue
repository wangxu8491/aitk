<template>
  <lemon-imui
      :user="selfUser"
      ref="IMUI"
      :width="900"
      :hide-message-name="false"
      :hide-message-time="false"
      :avatarCricle="true"
      @change-contact="handleChangeContact"
      @pull-messages="handlePullMessages"
      @send="handleSend"
      style="min-height:600px">
  </lemon-imui>
</template>
<script>
  import {getSelf, getModelList, handleMessage, pullMessage} from "./api"

  export default {
    components: {},
    data() {
      return {
        selfUser: {},
        path: "ws://localhost:8080/websocket/1",
        ws: {}
      };
    },
    computed: {},
    watch: {},
    created() {
    },
    mounted() {
      getSelf(null, response => {
        this.selfUser = response.data;
      });
      this.initWs();
      const IMUI = this.$refs.IMUI;
      getModelList(null, response => {
        IMUI.initContacts(response.data);
      });

      //IMUI.initEmoji(EmojiData);
      IMUI.initMenus([
        {
          name: "messages",
        },
        {
          name: "contacts",
        },
        {
          name: "custom1",
          title: "自定义按钮1",
          unread: 0,
          render: menu => {
            return ""//(<i class = "lemon-icon-attah"/>);
          },
          renderContainer: () => {
            return "";
          },
          isBottom: true,
        }
      ])
    },
    methods: {
      handlePullMessages(contact, next) {
        const {IMUI} = this.$refs;
        let param = {"modelId": contact.id};
        pullMessage(param, response => {
          next(response.data, true);
        })

      },

      handleChangeContact() {
      },
      handleSend(message, next, file) {
        console.log(message);
        let formData = new FormData();
        formData.append('file', file);
        formData.append('text', message.content)
        formData.append("userId", message.fromUser.id);
        formData.append("modelId", message.toContactId);
        handleMessage(formData, response => {
        });
        setTimeout(() => {
          next();
        }, 1000);
      },

      initWs() {
        this.ws = new WebSocket(this.path)
        this.ws.onopen = () => {
          console.log('ws连接状态：' + this.ws.readyState);
          this.ws.send('连接成功');
        }

        this.ws.onmessage = (data) => {
          console.log(data.data)
          const {IMUI} = this.$refs;
          IMUI.appendMessage(JSON.parse(data.data));
        }
        this.ws.onclose = () => {
          //监听整个过程中websocket的状态
          console.log('ws连接状态：' + this.ws.readyState);
        }

        this.ws.onerror = function (error) {
          console.log(error);
        }
      },
      closeWs() {
        this.ws.close();
      }
    },
  };
</script>
<style lang="stylus">
  .slot-group
    width 170px
    border-left 1px solid #ddd;
    height 100%
    box-sizing border-box
    padding 10px

    .slot-search
      margin 5px 0

  .slot-group-notice
    color #999
    padding 6px 0
    font-size 12px

  .slot-group-title
    font-size 12px

  .slot-group-member
    font-size 12px
    line-height 18px

  .slot-group-menu span
    display inline-block
    cursor pointer
    color #888
    margin 4px 10px 0 0
    border-bottom 2px solid transparent;

    &:hover
      color #000
      border-color #333

  .slot-contact-fixedtop
    padding 10px
    border-bottom 1px solid #ddd

  .slot-search
    width 100%
    box-sizing border-box
    font-size 14px
    border 1px solid #bbb
    padding 5px 10px
</style>
<template>
    <div id="app">
        <el-container style="height: 100vh ;  display: flex;  border: 1px solid #eee">
            <el-header class="header">
                AITK
            </el-header>
            <el-container>
                <el-aside width="300px" class="sidebar">
                    <el-input
                            placeholder="请输入搜索内容"
                            v-model="filterText"
                            @input="handleFilter"
                            class="text"
                    ></el-input>
                    <el-tree
                            :data="menuData"
                            :props="defaultProps"
                            :filter-node-method="filterNode"
                            ref="tree"
                            :default-expand-all="true"
                            @node-click="handleNodeClick"
                            class="custom-tree"
                    ></el-tree>
                </el-aside>
                <el-main class="main-content">
                    <router-view></router-view>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script>
    import {getModelTreeData} from "./api"

    export default {
        data() {
            return {
                filterText: '', // 搜索文本  ,
                menuData: [],
                defaultProps: {
                    children: 'children',
                    label: 'label'
                }
            };
        },
        methods: {
            handleNodeClick(data) {
                if (data.path) {
                    if (data.path == 'LLM') {
                        this.$router.push("/llm");
                    } else {
                        this.$router.push("/common");
                    }

                }
            },
            // 过滤树节点的方法
            filterNode(value, data) {
                if (!value) return true; // 如果没有搜索文本，显示所有节点
                return data.label.toLowerCase().includes(value.toLowerCase()); // 不区分大小写的包含检查
            },
            handleFilter() {
                this.$refs.tree.filter(this.filterText);
            }
        },
        created() {
            getModelTreeData(null, resp => {
                this.menuData = resp.data;
            });
        }
    };
</script>

<style scoped>
    .text {
        padding-bottom: 10px;
        padding-top: 20px;
    }

    .sidebar {
        background-color: rgb(238, 241, 246);
        color: #333;
        height: 100%;
    }

    .header {
        background-color: #409EFF;
        color: #333;
        line-height: 60px;
    }

    /*.custom-tree /deep/ .el-tree-node__content {*/
    /*    color: #22181c; !* 设置节点文本颜色为黑色 *!*/
    /*}*/


</style>
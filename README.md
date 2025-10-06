# BHRank - 玩家头衔管理系统

一个功能强大的 Minecraft 服务器玩家头衔管理插件，支持自定义头衔、颜色代码和权限管理。

## 下载
- 1.(推荐)[Modrinth] https://modrinth.com/plugin/bhrank
- 2.(高级)克隆本仓库手动编译

## 安装方法
- 1.将插件放入服务器的 `plugins` 文件夹
- 2.重启服务器
- 3.根据需要配置权限和设置
- 4.使用PAPI扩展获取玩家头衔 https://github.com/BlackHoleEra-Team/BhRankExpansion

## 支持版本
Minecraft 1.21+
**需要 Bukkit/Spigot/Paper 服务端**

## 功能特色

🎖️ **个性化头衔系统**
- 支持自定义玩家头衔
- 颜色代码支持（&符号）
- 智能长度限制计算（颜色代码不计入长度）

⚙️ **灵活的权限管理**
- 管理员权限：管理所有玩家头衔
- VIP权限：玩家可自定义自己的头衔
- 详细的权限节点控制

📊 **智能长度管理**
- 可配置最大头衔长度
- 实时显示可用字符数
- 自动去除颜色代码计算实际长度

🛠️ **便捷的命令系统**
- 完整的命令补全
- 详细的帮助信息
- 多子命令支持

## 命令列表

### 管理员命令
- `/br give <玩家> <头衔>` - 给玩家设置头衔
- `/br remove <玩家>` - 移除玩家的头衔  
- `/br info <玩家>` - 查询玩家头衔信息

### VIP玩家命令
- `/br set <头衔>` - 设置自己的头衔
- `/br clear` - 清除自己的称号

### 通用命令
- `/br help` - 显示帮助信息

## 权限节点

- `bherank.use` - 使用头衔管理命令（默认OP）
- `bherank.vip` - VIP玩家设置自己头衔的权限

## 配置说明

在 `config.yml` 中可配置：
```yaml
# 最大称号长度（可见字符数，颜色代码不计入）
max-title-length: 15
```

## 使用示例
### 请确保你已经加载<a href="https://github.com/BlackHoleEra-Team/BhRankExpansion">PAPI扩展</a>
**使用/br give <玩家> <头衔> 或 /br set <头衔>后**
### TrChat使用示例：
- 1.打开plugins/TrChat/channels/Normal.yml
- 2.找到以下字段
```
text: '&8[&3%player_world%&8]' # 这里
```
**改为**
```
text: '&8[&3%player_world%&8]%bhrank_player_title%'
```

**TAB插件使用方法类似，在groups.yml里面修改成员组相关显示文本即可**
---

## 开发者API
> ⚠️ **未经测试警告**: 此开发者API尚未经过完整测试，可能包含未知问题。不保证完全可用，推荐使用PAPI扩展
> 如经过验证，请通过issues反馈，谢谢
其他插件可以通过静态方法获取玩家头衔：
```
BHRank.getInstance().getPlayerTitle(playerUUID);
```


---

**开发团队**: BlackHoleEraTeam  
**问题反馈**: [GitHub Issues] https://github.com/BlackHoleEra-Team/BHRank/issues

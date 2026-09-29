using System.Collections.Generic;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace TbsSdk.Auth
{
    /// <summary>
    /// 当前登录用户信息（对齐后端 UserInfoVO，来源于 GET /private/user/info）。
    /// <para>后端未承诺的字段一律保留在 <see cref="Extra"/> 中，避免强类型映射失真。</para>
    /// </summary>
    public sealed class UserInfoModel
    {
        /// <summary>用户数字 id。</summary>
        [JsonPropertyName("id")]
        public long? Id { get; set; }

        [JsonPropertyName("userName")]
        public string UserName { get; set; }

        [JsonPropertyName("nickName")]
        public string NickName { get; set; }

        [JsonPropertyName("avatar")]
        public string Avatar { get; set; }

        /// <summary>账号状态码。</summary>
        [JsonPropertyName("accountStatus")]
        public int? AccountStatus { get; set; }

        /// <summary>账号状态文案。</summary>
        [JsonPropertyName("accountStatusStr")]
        public string AccountStatusStr { get; set; }

        /// <summary>可用角色组列表（含展平前的权限组结构）。</summary>
        [JsonPropertyName("avaliableRoles")]
        public List<RoleGroupModel> AvaliableRoles { get; set; }

        /// <summary>可用部门列表。</summary>
        [JsonPropertyName("avaliableDepartments")]
        public List<DepartmentModel> AvaliableDepartments { get; set; }

        /// <summary>当前选中部门。</summary>
        [JsonPropertyName("currentDepartment")]
        public DepartmentModel CurrentDepartment { get; set; }

        /// <summary>其余未映射字段。</summary>
        [JsonExtensionData]
        public IDictionary<string, JsonElement> Extra { get; set; }

        /// <summary>
        /// 展平全部角色组内的权限项列表（对齐前端 flatPermissions 逻辑）。
        /// </summary>
        public List<string> FlattenPermissions()
        {
            var permissions = new List<string>();
            if (AvaliableRoles == null)
            {
                return permissions;
            }
            foreach (var group in AvaliableRoles)
            {
                if (group?.Permissions == null)
                {
                    continue;
                }
                foreach (var permission in group.Permissions)
                {
                    // 鉴权编码为权限项的 role 字段（对齐前端 getMenuRoleCodes 取 role）
                    if (permission != null && !string.IsNullOrEmpty(permission.Role))
                    {
                        permissions.Add(permission.Role);
                    }
                }
            }
            return permissions;
        }
    }

    /// <summary>
    /// 角色组视图（后端 SystemRoleGroupVO：group 为角色组实体，permissions 为权限项对象列表）。
    /// <para>注意：角色组 id/名称在 group 子对象内，权限项为结构化对象（非字符串）。</para>
    /// </summary>
    public sealed class RoleGroupModel
    {
        /// <summary>角色组信息（SystemRoleGroupEntity）。</summary>
        [JsonPropertyName("group")]
        public RoleGroupInfo Group { get; set; }

        /// <summary>角色组下的权限项列表（SystemRoleEntity）。</summary>
        [JsonPropertyName("permissions")]
        public List<RolePermissionModel> Permissions { get; set; }

        [JsonExtensionData]
        public IDictionary<string, JsonElement> Extra { get; set; }

        /// <summary>角色组主键（取自 group）。</summary>
        public long? GetId() => Group?.Id;

        /// <summary>角色组名称（取自 group.roleGroupName）。</summary>
        public string GetName() => Group?.RoleGroupName;
    }

    /// <summary>
    /// 角色组信息（后端 SystemRoleGroupEntity，未承诺字段以 Extra 兜底）。
    /// </summary>
    public sealed class RoleGroupInfo
    {
        [JsonPropertyName("id")]
        public long? Id { get; set; }

        /// <summary>角色组名称（后端列 role_group_name）。</summary>
        [JsonPropertyName("roleGroupName")]
        public string RoleGroupName { get; set; }

        [JsonPropertyName("description")]
        public string Description { get; set; }

        [JsonPropertyName("flag")]
        public int? Flag { get; set; }

        [JsonExtensionData]
        public IDictionary<string, JsonElement> Extra { get; set; }
    }

    /// <summary>
    /// 权限项（后端 SystemRoleEntity，权限编码为 role 字段，未承诺字段以 Extra 兜底）。
    /// </summary>
    public sealed class RolePermissionModel
    {
        [JsonPropertyName("id")]
        public long? Id { get; set; }

        /// <summary>权限编码（唯一标识，鉴权比对用）。</summary>
        [JsonPropertyName("role")]
        public string Role { get; set; }

        [JsonPropertyName("name")]
        public string Name { get; set; }

        [JsonPropertyName("type")]
        public int? Type { get; set; }

        [JsonPropertyName("description")]
        public string Description { get; set; }

        [JsonPropertyName("flag")]
        public int? Flag { get; set; }

        [JsonPropertyName("dataScope")]
        public int? DataScope { get; set; }

        [JsonExtensionData]
        public IDictionary<string, JsonElement> Extra { get; set; }
    }

    /// <summary>
    /// 部门视图（字段名以后端实际返回为准，未命中字段保留在 Extra）。
    /// </summary>
    public sealed class DepartmentModel
    {
        [JsonPropertyName("id")]
        public long? Id { get; set; }

        [JsonPropertyName("departmentId")]
        public long? DepartmentId { get; set; }

        [JsonPropertyName("departmentName")]
        public string DepartmentName { get; set; }

        [JsonPropertyName("name")]
        public string Name { get; set; }

        [JsonExtensionData]
        public IDictionary<string, JsonElement> Extra { get; set; }

        /// <summary>取主键（id 与 departmentId 兜底）。</summary>
        public long? GetId()
        {
            return Id ?? DepartmentId;
        }

        /// <summary>取名称（departmentName 与 name 兜底）。</summary>
        public string GetName()
        {
            return DepartmentName ?? Name;
        }
    }
}

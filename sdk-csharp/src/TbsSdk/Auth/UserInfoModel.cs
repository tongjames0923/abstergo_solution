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
                if (group?.Permissions != null)
                {
                    permissions.AddRange(group.Permissions);
                }
            }
            return permissions;
        }
    }

    /// <summary>
    /// 角色组视图（后端 SystemRoleGroupVO，字段以 Extra 兜底）。
    /// </summary>
    public sealed class RoleGroupModel
    {
        [JsonPropertyName("id")]
        public long? Id { get; set; }

        [JsonPropertyName("name")]
        public string Name { get; set; }

        [JsonPropertyName("permissions")]
        public List<string> Permissions { get; set; }

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

define(["dojo/_base/declare", "dijit/_WidgetBase", "dijit/_TemplatedMixin", "dijit/_WidgetsInTemplateMixin", "dojo/text!./templates/exec.html", "dojo/_base/lang", "dojo/parser", "dojo/request", "dojo/dom-style", "dijit/registry", "dijit/form/Button", "dijit/form/TextBox", "dijit/form/Textarea", "dojo/store/Memory", "dijit/form/FilteringSelect", 'dojox/grid/DataGrid', 'dojo/data/ItemFileWriteStore'], function(declare, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, template, lang, parser, request, domStyle, registry, Button, TextBox, Textarea, Memory, FilteringSelect, DataGrid, ItemFileWriteStore) {
	return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {
		templateString: template,
		baseClass: "execWidget",
		sqlEditor: null,
		message: null,
		connectionSelectDom: null,
		connectionSelect: null,
		monitor: null,
		execSql: function() {
			var message = this.message;
			request.post("exec.json", {
				data: {
					sql: this.sqlEditor.value,
					connectionConfigId: this.connectionSelect.get('value')
				}
			}).then(function(text) {
				//				                var text="{success:true,\"message\":\"all data showed below:\",\"data\":[[\"ad_data\"],[\"ci_dwd_bh_adsl_log_enhance_yyyymmdd    aaaaaaaaaaaaaaaaa   bbbbbbbbbbbbbbbbbbbbb\"],[\"dim_industry\"],[\"li_ods_bh_adsl_log_decode_yyyymmdd\"],[\"li_ods_bh_adsl_log_out_yyyymmdd\"],[\"li_ods_bh_adsl_log_out_yyyymmdd_2\"],[\"lx_ad_data\"],[\"lx_li_ods_bh_adsl_log_decode_yyyymmdd\"],[\"lx_li_ods_bh_adsl_log_out_yyyymmdd\"],[\"ui_mobile_user_src_shdx_ad\"],[\"yalian_dpi_cdr_3day\"],[\"yalian_dpi_cdr_3day_o\"],[\"ydid_src\"],[\"yk_cookie_yyyymmdd\"]],\"meta\":{\"columnCount\":1,\"columnNames\":[\"tab_name\"]}}";
				var result = dojo.fromJson(text);
				if (!result.success) {
					message.innerHTML = "";
				} else {
					if (result.data != null) {
						message.innerHTML = result.message;
						var data = {
							identifier: "id",
							items: []
						};

						for (var i = 0; i < result.data.length; i++) {
							var row = {
								id: i + 1
							};
							for (var j = 0; j < result.meta.columnCount; j++) {
								row[result.meta.columnNames[j]] = result.data[i][j];
							}
							data.items.push(row)
						}
						var formatter = function(val) {
							var w = new TextBox({
								value: val
							});
							domStyle.set(w.domNode, "width", "100%");
							domStyle.set(w.domNode, "border", "0px");
							w._destroyOnRemove = true;
							return w;
						}
						var layout = [[{
							name: "",
							field: "id",
							width: '30px',
							formatter: formatter
						}]];
						for (var j = 0; j < result.meta.columnCount; j++) {
							layout[0].push({
								name: result.meta.columnNames[j],
								field: result.meta.columnNames[j],
								formatter: formatter
							});
						}
						var store = new ItemFileWriteStore({
							data: data
						});
						var grid = registry.byId("resultGrid");
						if (grid) {
							grid.destroy();
						}
						var grid = new DataGrid({
							id: "resultGrid",
							store: store,
							structure: layout,
							autoHeight: true,
							autoWidth: true,
							selectable: true
						});
						grid.placeAt("gridDiv");
						grid.startup();
						alert('ok')
					} else {
						message.innerHTML = "updated " + result.updateCount + " rows";
					}
				}
			});
		},
		postCreate: function() {
			this.inherited(arguments);
			var self = this;
			request.post("connection_config.json", {}).then(function(text) {
				var stateStore = new Memory({
					data: dojo.fromJson(text)
				})
				self.connectionSelect = new FilteringSelect({
					style: {
						width: "500px"
					},
					store: stateStore,
					searchAttr: "name",
					onChange: function(id, b) {
						self.monitor.innerHTML = "<a href=\"" + stateStore.get(id).monitorLink + "\">监控页面</a>"
					}
				},
				self.connectionSelectDom);
				if (stateStore.data != null && stateStore.data.length > 0) {
					self.connectionSelect.set("value", stateStore.getIdentity(stateStore.data[0]));
				}
			});
		}
	});
});


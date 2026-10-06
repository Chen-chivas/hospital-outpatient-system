#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Generate an improved requirements-analysis DOCX for Wang Ying's module,
based on the provided DOCX template, and avoiding the issues listed in
`需求分析问题.pdf` (e.g. leaving template text, wrong references, messy formatting).

This script:
1) Copies `【模板1】：需求分析.docx`
2) Replaces cover placeholders (project name / student / date)
3) Removes the sample body content
4) Inserts the improved content with template heading styles
"""

from __future__ import annotations

import copy
import sys
import zipfile
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, Optional
from xml.etree import ElementTree as ET


W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
XML_NS = "http://www.w3.org/XML/1998/namespace"

ET.register_namespace("w", W_NS)


def w(tag: str) -> str:
    return f"{{{W_NS}}}{tag}"


def iter_text_nodes(elem: ET.Element) -> Iterable[ET.Element]:
    for node in elem.iter():
        if node.tag == w("t"):
            yield node


def paragraph_plain_text(p: ET.Element) -> str:
    parts: list[str] = []
    for t in p.iter(w("t")):
        if t.text:
            parts.append(t.text)
    return "".join(parts)


def make_paragraph(text: str, style_id: str) -> ET.Element:
    p = ET.Element(w("p"))

    p_pr = ET.SubElement(p, w("pPr"))
    p_style = ET.SubElement(p_pr, w("pStyle"))
    p_style.set(w("val"), style_id)

    r = ET.SubElement(p, w("r"))
    t = ET.SubElement(r, w("t"))
    t.set(f"{{{XML_NS}}}space", "preserve")
    t.text = text
    return p


@dataclass(frozen=True)
class Para:
    text: str
    style: str  # styleId in template: heading 1 = "1", heading 2 = "2", heading 3 = "3", Normal = "a"


def replace_paragraph_text(p: ET.Element, new_text: str) -> None:
    """
    Replace the visible text of a paragraph while trying to preserve its run formatting.

    Word may split visible text into multiple runs/text nodes unpredictably, so we avoid
    naive string replacement on w:t nodes.
    """

    # Capture first run properties (font/size/bold etc.) if present.
    first_run = None
    for child in list(p):
        if child.tag == w("r"):
            first_run = child
            break

    rpr_copy = None
    if first_run is not None:
        rpr = first_run.find(w("rPr"))
        if rpr is not None:
            rpr_copy = copy.deepcopy(rpr)
            # The template uses red (FF0000) to highlight placeholders on the cover.
            # The course requirements doc warns that the cover should be black.
            color = rpr_copy.find(w("color"))
            if color is not None and color.get(w("val")) == "FF0000":
                color.set(w("val"), "000000")

    # Remove all runs
    for child in list(p):
        if child.tag == w("r"):
            p.remove(child)

    # Add new run with preserved rPr
    r = ET.SubElement(p, w("r"))
    if rpr_copy is not None:
        r.append(rpr_copy)
    t = ET.SubElement(r, w("t"))
    t.set(f"{{{XML_NS}}}space", "preserve")
    t.text = new_text


def normalize_label(s: str) -> str:
    return "".join(s.split())


def first_paragraph(elem: ET.Element) -> Optional[ET.Element]:
    for node in elem.iter():
        if node.tag == w("p"):
            return node
    return None


def fill_table_labels(root_elem: ET.Element, label_to_value: dict[str, str]) -> None:
    """
    In the provided template, the cover page is implemented as a table where:
    - left cell contains a label paragraph (e.g. '项目名称：')
    - right cell contains the value paragraph to be filled.
    """

    for tbl in root_elem.iter(w("tbl")):
        for tr in tbl.iter(w("tr")):
            tcs = [c for c in list(tr) if c.tag == w("tc")]
            if len(tcs) < 2:
                continue
            for idx, tc in enumerate(tcs[:-1]):
                p_label = first_paragraph(tc)
                if p_label is None:
                    continue
                label = normalize_label(paragraph_plain_text(p_label))
                if label not in label_to_value:
                    continue
                p_value = first_paragraph(tcs[idx + 1])
                if p_value is None:
                    continue
                replace_paragraph_text(p_value, label_to_value[label])


def build_body_paragraphs() -> list[Para]:
    H1 = "1"
    H2 = "2"
    H3 = "3"
    N = "a"

    p: list[Para] = []

    # 1 前言
    p += [
        Para("1、前言", H1),
        Para("1.1 编写目的", H2),
        Para(
            "本文档为《医院门诊管理系统》中“用户管理与权限控制、数据统计与分析”模块的需求分析说明书，"
            "用于统一团队对模块目标、范围边界、业务规则、接口交互、数据口径与质量要求的理解，"
            "为后续概要/详细设计、编码实现、测试验收与联调对接提供依据。",
            N,
        ),
        Para("1.2 读者对象", H2),
        Para("（1）项目管理人员：确认范围边界、里程碑与验收口径。", N),
        Para("（2）开发人员：依据功能与接口需求完成实现与联调。", N),
        Para("（3）测试人员：据此编写测试用例，覆盖主流程与异常场景。", N),
        Para("（4）业务代表（医生/护士/收费员/药师/管理员）：确认流程与权限符合实际。", N),
        Para("（5）运维/维护人员：了解日志审计、数据备份与常见故障处理点。", N),
        Para("1.3 术语与缩写解释", H2),
        Para("RBAC：基于角色的访问控制（Role-Based Access Control）。", N),
        Para("权限点：系统可授权的最小操作单元（接口级/按钮级）。", N),
        Para("数据权限：数据可见范围控制（全院/科室/个人）。", N),
        Para("指标口径：统计指标的计算规则、过滤规则与状态定义。", N),
        Para("KPI：关键指标（Key Performance Indicator）。", N),
        Para("审计日志：对关键操作的可追溯记录（操作者/对象/结果/时间等）。", N),
        Para("幂等：重复提交同一请求不应造成重复扣费/重复生成记录。", N),
        Para("1.4 流程图标定义", H2),
        Para(
            "本模块流程图符号参照课程统一模板：开始/结束、操作、判断、数据、连接、打印、自动/手工等。",
            N,
        ),
        Para("1.5 参考资料", H2),
        Para("（1）GB/T 8567-2006《计算机软件文档编制规范》。", N),
        Para("（2）GB/T 9385-2008《计算机软件需求规格说明规范》。", N),
        Para("（3）行业参考：《医院信息系统（HIS）基本功能规范》。", N),
        Para("（4）课程要求：《应用软件综合课程设计 I》实验指导与评分标准。", N),
    ]

    # 2 任务概述
    p += [
        Para("2、任务概述", H1),
        Para("2.1 模块定位与范围边界", H2),
        Para(
            "本模块面向系统整体提供两类能力："
            "（1）用户与权限管理：统一账号、角色、权限点与审计日志，为挂号/就诊/收费/药房/统计等模块提供安全基础；"
            "（2）数据统计与分析：基于全系统业务数据形成指标统计、维度分析、趋势对比、看板与导出能力，支撑运营与管理决策。",
            N,
        ),
        Para("本模块不负责其它业务模块的业务录入本体，但需要约定数据口径与接口同步要求。", N),
        Para("2.2 业务参与角色（与权限关联）", H2),
        Para("管理员：账号/角色/权限维护、统计全院查看、审计查看。", N),
        Para("医生：查看个人工作量/统计，必要时查看科室汇总（按授权）。", N),
        Para("护士：查看科室流程相关统计（按授权），不接触敏感字段。", N),
        Para("收费员：查看收费/退费对账统计，打印与导出需授权并留痕。", N),
        Para("药师：查看处方/发药/库存相关统计，导出需授权并留痕。", N),
        Para("2.3 模块核心流程", H2),
        Para("2.3.1 流程图", H3),
        Para("图2-1 用户与权限管理流程图（管理员维护→授权→审计）。", N),
        Para("图2-2 统计分析查询流程图（筛选→聚合→展示→导出/打印→留痕）。", N),
        Para("2.3.2 流程说明", H3),
        Para(
            "流程启动：用户登录系统后，系统根据账号状态与角色权限加载可用菜单与功能。"
            "管理员可进入用户与权限管理维护账号、角色与权限；业务人员可进入统计模块查看与导出授权范围内的数据。",
            N,
        ),
        Para(
            "数据获取：统计模块从挂号、就诊、收费、药房等模块获取业务数据（可通过接口同步或数据库查询实现），"
            "并按照指标口径完成聚合计算；导出与打印动作须记录审计日志。",
            N,
        ),
        Para("2.3.3 作业分解", H3),
        Para("作业代码：UM-01；作业描述：用户登录与会话鉴权；操作员：所有用户；注释：登录成功后才能访问功能。", N),
        Para("作业代码：UM-02；作业描述：账号与角色维护；操作员：管理员；注释：新增/编辑/禁用/重置密码/授权。", N),
        Para("作业代码：UM-03；作业描述：审计日志记录与查询；操作员：系统/管理员；注释：关键操作自动留痕。", N),
        Para("作业代码：SA-01；作业描述：统计指标查询；操作员：授权用户；注释：按时间/科室/医生等筛选。", N),
        Para("作业代码：SA-02；作业描述：图表与看板展示；操作员：授权用户；注释：趋势/对比/占比/热力图。", N),
        Para("作业代码：SA-03；作业描述：报表导出与打印；操作员：授权用户；注释：Excel/PDF 导出并写审计日志。", N),
    ]

    # 3 用例分析
    p += [
        Para("3、用例分析", H1),
        Para("3.1 用例图", H2),
        Para("图3-1 用户与权限管理用例图（管理员/业务人员/系统）。", N),
        Para("图3-2 数据统计与分析用例图（查询/看板/导出/权限控制）。", N),
        Para("3.2 用例说明", H2),
        Para("3.2.1 用户与权限管理相关用例", H3),
        Para("（1）“创建用户账号”用例", N),
        Para("功能编号：UM-UC-001", N),
        Para("功能名称：创建用户账号", N),
        Para("用户：管理员", N),
        Para("数据来源：管理员输入、系统字典（科室/角色/权限点）", N),
        Para("前置条件：管理员登录成功，拥有 UM_USER_CREATE 权限点", N),
        Para("输入数据：用户名/姓名/工号（或患者ID）、手机号、所属科室、角色集合、账号状态", N),
        Para(
            "处理：系统校验字段合法性与唯一性；保存用户信息；分配角色；生成初始密码（或重置链接）；写入审计日志。",
            N,
        ),
        Para("后置条件：用户账号可被登录鉴权模块识别；权限立即生效；审计日志可追溯。", N),
        Para("说明：初始密码需强制首次登录修改；敏感字段入库需加密、页面展示需脱敏。", N),
        Para("（2）“角色授权与权限点配置”用例", N),
        Para("功能编号：UM-UC-002", N),
        Para("功能名称：角色授权与权限点配置", N),
        Para("用户：管理员", N),
        Para("前置条件：管理员拥有 UM_ROLE_GRANT 权限点", N),
        Para(
            "处理：管理员为角色配置权限点集合；系统校验权限点合法；保存后立即生效；记录前后差异到审计日志。",
            N,
        ),
        Para("后置条件：角色权限变更后，相关用户重新登录或刷新权限即可生效（具体策略实现阶段确定）。", N),
        Para("（3）“登录鉴权与账号状态校验”用例", N),
        Para("功能编号：UM-UC-003", N),
        Para("功能名称：登录鉴权与账号状态校验", N),
        Para("用户：所有角色用户", N),
        Para("前置条件：用户账号存在且未被禁用/锁定", N),
        Para("处理：校验凭证；校验账号状态；生成会话；加载菜单与权限点；记录登录日志。", N),
        Para("后置条件：用户可访问授权功能；非授权访问被拒绝并记录安全日志。", N),
        Para("3.2.2 数据统计与分析相关用例", H3),
        Para("（1）“统计查询与筛选”用例", N),
        Para("功能编号：SA-UC-001", N),
        Para("功能名称：统计查询与筛选", N),
        Para("用户：医生/护士/收费员/药师/管理员（按授权）", N),
        Para("前置条件：用户已登录且拥有 STAT_READ 权限点", N),
        Para("输入数据：时间范围、科室（可选）、医生（可选）、渠道（可选）、费用项目（可选）", N),
        Para(
            "处理：系统按指标口径聚合数据；按数据权限过滤可见范围；返回趋势/对比/排行/占比等结构化结果。",
            N,
        ),
        Para("后置条件：统计结果在页面展示，并可切换图表类型。", N),
        Para("说明：默认时间范围为最近 7 天；最大时间跨度与最大返回行数可配置。", N),
        Para("（2）“报表导出（Excel/PDF）”用例", N),
        Para("功能编号：SA-UC-002", N),
        Para("功能名称：报表导出（Excel/PDF）", N),
        Para("用户：拥有 STAT_EXPORT 权限点的用户", N),
        Para("前置条件：已完成统计查询；导出权限校验通过", N),
        Para("处理：系统按当前筛选条件生成导出文件；限制导出数据量；记录导出审计日志（含筛选条件摘要）。", N),
        Para("后置条件：导出文件生成成功并可下载/保存；失败时提示原因并可重试。", N),
        Para("图编号要求：所有流程图/用例图均需在图下方按模板样式编号，例如“图3-1 用户与权限管理用例图”。", N),
    ]

    # 4 统计模块说明（指标口径与展示）
    p += [
        Para("4、统计模块说明（指标口径与展示）", H1),
        Para("4.1 核心指标定义（口径）", H2),
        Para("（1）门诊量：统计周期内“接诊完成”的就诊记录数（以就诊模块状态为准）。", N),
        Para("（2）挂号数：统计周期内“支付成功”的挂号订单数（以挂号+收费状态为准）。", N),
        Para("（3）缴费人次：统计周期内支付成功的缴费记录数（含挂号费/诊疗费/药费等）。", N),
        Para("（4）总收入：支付成功金额合计 − 退费成功金额合计。", N),
        Para("（5）处方数：统计周期内“已生效”的处方数量（以就诊模块处方状态为准）。", N),
        Para("4.2 统计维度", H2),
        Para("时间维度：小时/日/周/月/季度/年。", N),
        Para("科室维度：按科室统计门诊量、收入、工作量等。", N),
        Para("医生维度：按医生统计接诊量、处方量、均次费用（按数据权限控制）。", N),
        Para("患者维度：年龄段、性别、医保/自费等类型。", N),
        Para("费用维度：费用项目结构、支付方式结构。", N),
        Para("4.3 展示与可视化", H2),
        Para("折线图：门诊量/收入趋势。", N),
        Para("柱状图/条形图：科室/医生 TOPN 对比排行。", N),
        Para("饼图/环形图：费用结构、患者类型占比。", N),
        Para("热力图：门诊高峰时段识别（小时×日期/科室）。", N),
        Para("数字看板：今日核心指标与环比/同比提示（可选）。", N),
        Para("4.4 导出与打印", H2),
        Para("支持导出 Excel/PDF；导出与打印均需鉴权并记录审计日志；默认限制时间跨度与数据量。", N),
    ]

    # 5 非功能需求
    p += [
        Para("5、模块非功能需求", H1),
        Para("5.1 性能需求", H2),
        Para("统计页面响应时间 ≤ 1 秒（常规查询）；聚合查询 ≤ 2 秒（大查询）。", N),
        Para("支持 ≥ 50 并发用户访问统计查询（课程演示场景）。", N),
        Para("5.2 可靠性需求", H2),
        Para("关键操作（支付/退费/导出/权限变更）需记录审计日志，异常需可追溯。", N),
        Para("数据同步失败需可重试；统计口径以最终一致的业务状态为准。", N),
        Para("5.3 易用性需求", H2),
        Para("界面布局符合院内使用习惯；筛选条件清晰；错误提示可理解可操作。", N),
        Para("5.4 安全性需求", H2),
        Para("敏感信息（身份证/手机号等）加密存储、脱敏显示；导出需授权并留痕。", N),
        Para("权限严格分级：未授权不得访问接口与数据；越权访问需拦截并记录安全日志。", N),
        Para("5.5 兼容性需求", H2),
        Para("支持 Windows 主流操作系统；兼容医院现有打印机设备（如实现打印）。", N),
    ]

    # 6 接口与数据要求（与其他模块对接）
    p += [
        Para("6、接口与数据要求（与其他模块对接）", H1),
        Para("6.1 与挂号模块接口", H2),
        Para("同步挂号订单（含渠道、科室、医生、时段、状态、金额）用于挂号数/渠道分析。", N),
        Para("读取患者账号状态（黑名单/禁用）用于挂号资格校验（由挂号模块调用用户模块）。", N),
        Para("6.2 与就诊模块接口", H2),
        Para("同步接诊记录（开始/结束时间、科室、医生、诊断、处方状态）用于门诊量/工作量统计。", N),
        Para("6.3 与收费模块接口", H2),
        Para("同步支付流水与退费记录，用于收入、退费率与费用结构统计。", N),
        Para("6.4 与药房模块接口", H2),
        Para("同步处方审核/发药记录与药品消耗，用于药品统计与库存预警分析（如实现）。", N),
    ]

    # 7 验收标准
    p += [
        Para("7、验收标准", H1),
        Para("7.1 功能验收", H2),
        Para("用户管理：支持账号创建/禁用/重置密码/角色授权；接口鉴权有效；关键操作审计可查。", N),
        Para("统计分析：支持时间/科室/医生/渠道筛选；至少输出 5 个核心指标；支持图表展示与 Excel/PDF 导出。", N),
        Para("7.2 安全验收", H2),
        Para("未授权访问与越权访问均被拒绝；敏感信息默认脱敏；导出行为留痕可追溯。", N),
        Para("7.3 性能验收", H2),
        Para("在课程演示数据规模下，统计查询与导出满足本章性能指标。", N),
    ]

    return p


def main() -> int:
    if len(sys.argv) != 4:
        print(
            "Usage: python scripts/generate_wy_requirement_docx.py <template.docx> <output.docx> <issues_pdf>",
            file=sys.stderr,
        )
        return 2

    template_path = Path(sys.argv[1])
    output_path = Path(sys.argv[2])
    issues_pdf = Path(sys.argv[3])

    if not template_path.exists():
        print(f"Template not found: {template_path}", file=sys.stderr)
        return 2
    if not issues_pdf.exists():
        print(f"Issues PDF not found: {issues_pdf}", file=sys.stderr)
        return 2

    # Read template parts
    with zipfile.ZipFile(template_path, "r") as zin:
        template_files = {name: zin.read(name) for name in zin.namelist()}

    doc_xml = template_files["word/document.xml"].decode("utf-8", errors="ignore")
    root = ET.fromstring(doc_xml)
    body = root.find(w("body"))
    if body is None:
        raise RuntimeError("Invalid template: missing w:body")

    # Preserve section properties (<w:sectPr>) from template.
    sect_pr = body.find(w("sectPr"))
    if sect_pr is None:
        # some docs store sectPr inside last paragraph; fallback: find any sectPr in body subtree
        sect_pr = next(iter(body.iter(w("sectPr"))), None)
    if sect_pr is None:
        raise RuntimeError("Invalid template: missing w:sectPr")

    # We rebuild body children: keep cover+TOC, then add our content, then sectPr.
    # Keep elements before the template's sample marker: "【以下文字内容是示例，仅供参考】"
    kept: list[ET.Element] = []
    sample_marker_found = False

    for child in list(body):
        if child.tag == w("sectPr"):
            continue

        keep = True
        if child.tag == w("p"):
            txt = paragraph_plain_text(child)
            if "【以下文字内容是示例，仅供参考】" in txt:
                sample_marker_found = True
                keep = False
            if sample_marker_found:
                keep = False

        if keep and not sample_marker_found:
            kept.append(child)

    # Fix cover values by labels (robust against run splitting / encoding issues).
    project_name = "医院门诊管理系统——用户管理、数据统计与分析"
    student_name = "王颖（2023112463）"
    submit_date = "2026年5月22日"

    label_to_value = {
        normalize_label("项目名称："): project_name,
        normalize_label("姓名（学号）："): student_name,
        normalize_label("提交日期："): submit_date,
    }

    # 1) Table-based cover fields (template uses tables)
    for elem in kept:
        fill_table_labels(elem, label_to_value)

    # 2) Paragraph-based cover fields (fallback)
    for i in range(len(kept) - 1):
        cur = kept[i]
        nxt = kept[i + 1]
        if cur.tag != w("p") or nxt.tag != w("p"):
            continue

        label = normalize_label(paragraph_plain_text(cur))
        if label == normalize_label("项目名称："):
            replace_paragraph_text(nxt, project_name)
        elif label == normalize_label("姓名（学号）："):
            replace_paragraph_text(nxt, student_name)
        elif label == normalize_label("提交日期："):
            replace_paragraph_text(nxt, submit_date)

    # 3) Replace any remaining template project-name occurrences (TOC results, etc.)
    #    This avoids the common mistake of leaving sample template text in the cover/目录.
    old_project = "食堂外卖系统——地址管理、用户下单、浏览商品、购物车、微信登录、历史订单"
    for elem in kept:
        for t in iter_text_nodes(elem):
            if not t.text:
                continue
            if old_project in t.text:
                t.text = t.text.replace(old_project, project_name)
            if "****（2023*****）" in t.text:
                t.text = t.text.replace("****（2023*****）", student_name)
            if "2026年**月**日" in t.text:
                t.text = t.text.replace("2026年**月**日", submit_date)

    # Clear body
    for child in list(body):
        body.remove(child)

    # Re-add kept (cover + TOC), then the improved content
    for elem in kept:
        body.append(elem)

    for para in build_body_paragraphs():
        body.append(make_paragraph(para.text, para.style))

    # Append original sectPr as last element
    body.append(copy.deepcopy(sect_pr))

    new_doc_xml = ET.tostring(root, encoding="utf-8", xml_declaration=True)

    # Write new docx (copy all parts, replace document.xml only)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output_path, "w", compression=zipfile.ZIP_DEFLATED) as zout:
        for name, data in template_files.items():
            if name == "word/document.xml":
                zout.writestr(name, new_doc_xml)
            else:
                zout.writestr(name, data)

    # Quick sanity checks (avoid the common mistakes list).
    with zipfile.ZipFile(output_path, "r") as zchk:
        out_xml = zchk.read("word/document.xml").decode("utf-8", errors="ignore")
    for forbidden in [
        "内容应该在此处",
        "模板此处有误",
        "食堂外卖系统",
        "【以下文字内容是示例，仅供参考】",
        "****（2023*****）",
        "2026年**月**日",
        "图3-1 XXXX",
    ]:
        if forbidden in out_xml:
            raise RuntimeError(f"Output still contains template placeholder text: {forbidden}")

    print(f"OK: generated {output_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

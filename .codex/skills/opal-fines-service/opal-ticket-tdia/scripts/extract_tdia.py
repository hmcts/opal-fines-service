#!/usr/bin/env python3
"""Extract OPAL TDIA content from saved Confluence HTML into markdown."""

from __future__ import annotations

import argparse
import re
import shutil
import sys
from collections import OrderedDict
from dataclasses import dataclass
from html.parser import HTMLParser
from pathlib import Path

KNOWN_HEADINGS = [
    'Status',
    'HMCTS Approvals',
    'Overview and Scope',
    'Design Sprint Tickets',
    'Design Collateral',
    'E2E Interactions',
    'Opal User Portal (FE)',
    'Pages',
    'Global Components',
    'Services (API Only)',
    'Feature Toggles',
    'Design Notes',
    'Payload Generation',
    'Opal Services (BE)',
    'REST API Endpoints',
    'Non-API Services',
    'Opal Database (DB)',
    'Tables',
    'Indexes',
    'Sequences',
    'Stored Procedures',
    'Data',
    'Azure Infrastructure',
    'Libra / GOB',
    'Stored Procedures and Gateway Actions',
    'ETL',
    'Test and QA',
    'Integration / Component Tests',
    'Frontend tests',
    'Backend tests',
    'E2E / Functional Tests',
    'Non-Functional Tests',
    'Automated Accessibility Tests',
    'Manual Accessibility Tests',
    'Release-based Testing',
    'Tech Concerns',
    'Tech Decisions',
    'Tech Debt',
    'Non-Functional Requirements',
    'Managed Data Set Configuration',
    'Response Time Targets',
    'Personal Data Processing Operations',
    'Non-Business Critical Applications / Components',
    'Specific NFRs',
    'Assumptions',
]

HEADING_LEVELS = {
    'Status': 1,
    'HMCTS Approvals': 1,
    'Overview and Scope': 1,
    'Design Sprint Tickets': 1,
    'Design Collateral': 1,
    'E2E Interactions': 1,
    'Opal User Portal (FE)': 1,
    'Opal Services (BE)': 1,
    'Opal Database (DB)': 1,
    'Azure Infrastructure': 1,
    'Libra / GOB': 1,
    'ETL': 1,
    'Test and QA': 1,
    'Tech Concerns': 1,
    'Tech Decisions': 1,
    'Tech Debt': 1,
    'Non-Functional Requirements': 1,
    'Assumptions': 1,
    'Pages': 2,
    'Global Components': 2,
    'Services (API Only)': 2,
    'Feature Toggles': 2,
    'Design Notes': 2,
    'Payload Generation': 2,
    'REST API Endpoints': 2,
    'Non-API Services': 2,
    'Tables': 2,
    'Indexes': 2,
    'Sequences': 2,
    'Stored Procedures': 2,
    'Data': 2,
    'Stored Procedures and Gateway Actions': 2,
    'Integration / Component Tests': 2,
    'E2E / Functional Tests': 2,
    'Non-Functional Tests': 2,
    'Automated Accessibility Tests': 3,
    'Manual Accessibility Tests': 3,
    'Release-based Testing': 3,
    'Managed Data Set Configuration': 2,
    'Response Time Targets': 2,
    'Personal Data Processing Operations': 2,
    'Non-Business Critical Applications / Components': 2,
    'Specific NFRs': 2,
    'Convert Account Page': 3,
    'Frontend tests': 3,
    'Backend tests': 3,
    'Performance Testing': 4,
    'Security Testing': 4,
    'Operational Acceptance Testing': 4,
}

DOCUMENT_PREAMBLE = 'Document Preamble'
VOID_TAGS = {'area', 'base', 'br', 'col', 'embed', 'hr', 'img', 'input', 'link', 'meta', 'source', 'track', 'wbr'}
IMPLICIT_HEADINGS = set(KNOWN_HEADINGS) | set(HEADING_LEVELS)


@dataclass
class SectionBlock:
    key: str
    title: str
    content: str
    position: int


@dataclass
class ImageRef:
    section_key: str
    source_path: str
    title: str | None = None


def normalize_inline(text: str) -> str:
    text = text.replace('\xa0', ' ')
    text = re.sub(r'[ \t\r\f\v]+', ' ', text)
    text = re.sub(r' *\n *', '\n', text)
    return text


def normalize_block(text: str) -> str:
    text = normalize_inline(text)
    text = re.sub(r'\n{3,}', '\n\n', text)
    return text.strip()


def normalize_cell(text: str) -> str:
    return normalize_block(text).replace('\n', '<br>')


def heading_level(title: str) -> int:
    return HEADING_LEVELS.get(title, 2)


def build_section_key(path_titles: list[str]) -> str:
    return ' > '.join(path_titles)


def sanitize_markdown(text: str) -> str:
    text = text.strip()
    if not text:
        return '_Section not found in extracted text._'
    return text.replace('\n#', '\n\\#')


def resolve_requested_sections(requested: list[str] | None) -> list[str]:
    if not requested:
        return []

    resolved: list[str] = []
    seen = set()
    for item in requested:
        if item in seen:
            continue
        seen.add(item)
        resolved.append(item)
    return resolved


def match_requested_sections(sections: dict[str, SectionBlock], selector: str) -> list[SectionBlock]:
    if selector in sections:
        return [sections[selector]]

    matched = [block for key, block in sections.items() if key.endswith(f' > {selector}') or block.title == selector]
    matched.sort(key=lambda block: (block.position, block.key))
    return matched


def build_output(
    source_path: Path,
    sections: dict[str, SectionBlock],
    requested: list[str],
    saved_from_url: str | None = None,
    e2e_image_markdown: str | None = None,
    e2e_image_missing_note: str | None = None,
    section_image_markdown: dict[str, list[str]] | None = None,
) -> str:
    if requested:
        resolved: list[SectionBlock] = []
        missing: list[str] = []

        for selector in requested:
            matched = match_requested_sections(sections, selector)
            if matched:
                resolved.extend(matched)
            else:
                missing.append(selector)
    else:
        resolved = sorted(sections.values(), key=lambda block: (block.position, block.key))
        missing = []

    unique_resolved: list[SectionBlock] = []
    seen_keys = set()
    for block in resolved:
        if block.key in seen_keys:
            continue
        seen_keys.add(block.key)
        unique_resolved.append(block)

    lines = [
        f'# TDIA Extraction: {source_path.name}',
        '',
        f'- Source: `{source_path}`',
        '- Input type: html',
    ]

    if saved_from_url:
        lines.append(f'- Saved from: {saved_from_url}')

    lines.extend(
        [
            f'- Sections requested: {"all" if not requested else len(requested)}',
            f'- Sections found: {len(unique_resolved)}',
        ]
    )

    if missing:
        lines.append(f'- Sections missing: {", ".join(missing)}')

    for block in unique_resolved:
        content = sanitize_markdown(block.content)
        image_markdown = []
        if section_image_markdown:
            image_markdown = section_image_markdown.get(block.key, [])

        if block.key == 'E2E Interactions':
            if image_markdown:
                content = f'{content}\n\n### Diagram Images\n\n' + '\n\n'.join(image_markdown)
            elif e2e_image_markdown:
                content = f'{content}\n\n### Diagram Image\n\n{e2e_image_markdown}'
            elif e2e_image_missing_note:
                content = f'{content}\n\n{e2e_image_missing_note}'
        lines.extend(['', f'## {block.key}', '', content])

    return '\n'.join(lines).rstrip() + '\n'


def derive_tdia_name(sections: dict[str, SectionBlock], fallback: str) -> str:
    preamble = sections.get(DOCUMENT_PREAMBLE)
    if preamble:
        lines = [line.strip() for line in preamble.content.splitlines() if line.strip()]
        for line in lines:
            tdia_match = re.match(r'^TDIA:\s*(.+)$', line, re.IGNORECASE)
            if tdia_match:
                return tdia_match.group(1).strip()

            design_match = re.search(r'\bfor\s+(.+)$', line, re.IGNORECASE)
            if 'tech design ia' in line.lower() and design_match:
                return design_match.group(1).strip()

    return fallback


def slugify(value: str) -> str:
    slug = value.strip().lower()
    slug = re.sub(r'[^a-z0-9]+', '-', slug)
    slug = re.sub(r'-{2,}', '-', slug).strip('-')
    return slug or 'tdia'


def copy_e2e_image(target_dir: Path, image_path: Path) -> tuple[Path, str]:
    image_dir = target_dir / 'images'
    image_dir.mkdir(parents=True, exist_ok=True)

    safe_name = f'e2e-interactions{image_path.suffix.lower()}'
    target_path = image_dir / safe_name
    shutil.copy2(image_path, target_path)
    markdown = f'![E2E Interactions](images/{target_path.name})'
    return target_path, markdown


def resolve_companion_asset(html_path: Path, source_path: str) -> Path | None:
    files_dir = html_path.with_name(f'{html_path.stem}_files')
    if not files_dir.exists():
        return None

    cleaned = source_path.strip()
    if cleaned.startswith('./'):
        cleaned = cleaned[2:]

    candidate = (html_path.parent / cleaned).resolve()
    if candidate.exists():
        return candidate

    fallback = files_dir / Path(cleaned).name
    if fallback.exists():
        return fallback.resolve()

    return None


def copy_section_images(
    target_dir: Path,
    html_path: Path,
    image_refs: list[ImageRef],
    allowed_sections: set[str] | None = None,
) -> dict[str, list[str]]:
    if not image_refs:
        return {}

    image_dir = target_dir / 'images'
    image_dir.mkdir(parents=True, exist_ok=True)

    by_section: dict[str, list[str]] = {}
    seen_targets: set[tuple[str, str]] = set()

    for image_ref in image_refs:
        if allowed_sections is not None and image_ref.section_key not in allowed_sections:
            continue

        source_asset = resolve_companion_asset(html_path, image_ref.source_path)
        if source_asset is None:
            continue

        target_name = source_asset.name
        target_path = image_dir / target_name
        key = (image_ref.section_key, target_name)
        if key not in seen_targets:
            shutil.copy2(source_asset, target_path)
            seen_targets.add(key)

        alt = image_ref.title or image_ref.section_key
        by_section.setdefault(image_ref.section_key, []).append(f'![{alt}](images/{target_name})')

    return by_section


def write_output_bundle(
    output_root: Path,
    source_path: Path,
    output_markdown: str,
    sections: dict[str, SectionBlock],
    e2e_image_path: Path | None = None,
) -> Path:
    tdia_name = derive_tdia_name(sections, fallback=source_path.stem)
    target_dir = output_root / slugify(tdia_name)
    target_dir.mkdir(parents=True, exist_ok=True)

    source_target = target_dir / f'source{source_path.suffix.lower()}'
    if source_path.resolve() != source_target.resolve():
        shutil.copy2(source_path, source_target)

    if e2e_image_path is not None:
        copy_e2e_image(target_dir, e2e_image_path)

    markdown_target = target_dir / 'extracted.md'
    markdown_target.write_text(output_markdown, encoding='utf-8')
    return markdown_target


def extract_saved_from_url(raw_html: str) -> str | None:
    match = re.search(r'<!--\s*saved from url=\(\d+\)(.*?)\s*-->', raw_html, re.IGNORECASE)
    if not match:
        return None
    return match.group(1).strip() or None


class ConfluenceHtmlSectionParser(HTMLParser):
    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.sections: OrderedDict[str, dict[str, object]] = OrderedDict()
        self.section_images: list[ImageRef] = []
        self.heading_stack: list[str] = []
        self.section_counter = 0
        self.in_body = False

        self.current_heading_level: int | None = None
        self.current_heading_text: list[str] = []
        self.orphan_fragments: list[str] = []

        self.current_paragraph: list[str] | None = None
        self.current_list_item: list[str] | None = None
        self.list_stack: list[dict[str, object]] = []

        self.current_table: list[tuple[list[str], bool]] | None = None
        self.current_row: list[tuple[str, bool]] | None = None
        self.current_cell: list[str] | None = None
        self.current_cell_is_header = False

        self.active_link: dict[str, object] | None = None

        self.ignore_depth = 0
        self.skip_depth = 0

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        attrs_dict = {key: value or '' for key, value in attrs}

        if tag == 'body':
            self.in_body = True
            return

        if not self.in_body:
            return

        if self.skip_depth:
            if tag not in VOID_TAGS:
                self.skip_depth += 1
            return

        if self.ignore_depth:
            if tag not in VOID_TAGS:
                self.ignore_depth += 1
            return

        if tag in {'script', 'style', 'template'}:
            self.ignore_depth = 1
            return

        if self.should_skip_subtree(attrs_dict):
            self.skip_depth = 1
            return

        if re.fullmatch(r'h[1-6]', tag):
            self.flush_orphan_fragments()
            self.flush_paragraph()
            self.flush_lists()
            self.flush_table()
            self.current_heading_level = int(tag[1])
            self.current_heading_text = []
            return

        if tag == 'p' and self.current_table is None:
            self.flush_orphan_fragments()
            self.current_paragraph = []
            return

        if tag in {'ul', 'ol'} and self.current_table is None:
            self.flush_orphan_fragments()
            self.list_stack.append({'ordered': tag == 'ol', 'items': []})
            return

        if tag == 'li' and self.current_table is None:
            self.current_list_item = []
            return

        if tag == 'table':
            self.flush_orphan_fragments()
            self.flush_paragraph()
            self.flush_lists()
            self.current_table = []
            return

        if tag == 'tr' and self.current_table is not None:
            self.current_row = []
            return

        if tag in {'th', 'td'} and self.current_row is not None:
            self.current_cell = []
            self.current_cell_is_header = tag == 'th'
            return

        if tag == 'a':
            self.active_link = {'href': attrs_dict.get('href', ''), 'text': []}
            return

        if tag == 'br':
            self.append_fragment('\n')
            return

        if tag == 'img':
            embedded_image = self.embedded_image_ref(attrs_dict)
            if embedded_image is not None:
                self.section_images.append(embedded_image)
                return

            macro_text = self.macro_text(attrs_dict)
            if macro_text:
                self.append_fragment(macro_text)

    def handle_endtag(self, tag: str) -> None:
        if tag == 'body':
            self.flush_orphan_fragments()
            self.in_body = False
            return

        if not self.in_body:
            return

        if self.skip_depth:
            self.skip_depth -= 1
            return

        if self.ignore_depth:
            self.ignore_depth -= 1
            return

        if tag == 'a' and self.active_link is not None:
            href = str(self.active_link.get('href', '')).strip()
            text = normalize_block(''.join(self.active_link.get('text', [])))
            rendered = ''
            if href and text:
                if text == href:
                    rendered = href
                else:
                    rendered = f'[{text}]({href})'
            elif text:
                rendered = text
            elif href:
                rendered = href
            self.active_link = None
            if rendered:
                self.append_fragment(rendered)
            return

        if re.fullmatch(r'h[1-6]', tag) and self.current_heading_level is not None:
            title = normalize_block(''.join(self.current_heading_text))
            if title:
                self.start_section(title, self.current_heading_level)
            self.current_heading_level = None
            self.current_heading_text = []
            return

        if tag == 'p' and self.current_table is None:
            self.flush_paragraph()
            return

        if tag == 'li' and self.current_list_item is not None:
            item = normalize_block(''.join(self.current_list_item))
            if item and self.list_stack:
                self.list_stack[-1]['items'].append(item)
            self.current_list_item = None
            return

        if tag in {'ul', 'ol'} and self.list_stack:
            list_state = self.list_stack.pop()
            items = list_state['items']
            if items:
                ordered = bool(list_state['ordered'])
                prefix = lambda index: f'{index}. ' if ordered else '- '
                block = '\n'.join(f'{prefix(index)}{item}' for index, item in enumerate(items, start=1))
                self.emit_block(block)
            return

        if tag in {'th', 'td'} and self.current_cell is not None and self.current_row is not None:
            cell_text = normalize_cell(''.join(self.current_cell))
            self.current_row.append((cell_text, self.current_cell_is_header))
            self.current_cell = None
            self.current_cell_is_header = False
            return

        if tag == 'tr' and self.current_row is not None and self.current_table is not None:
            if any(cell for cell, _ in self.current_row):
                row_cells = [cell for cell, _ in self.current_row]
                is_header = any(is_header for _, is_header in self.current_row)
                self.current_table.append((row_cells, is_header))
            self.current_row = None
            return

        if tag == 'table':
            self.flush_table()

    def handle_data(self, data: str) -> None:
        if not self.in_body or self.skip_depth or self.ignore_depth:
            return
        self.append_fragment(data)

    def should_skip_subtree(self, attrs: dict[str, str]) -> bool:
        element_id = attrs.get('id', '')
        element_class = attrs.get('class', '')
        return element_id.startswith('give-freely-root') or 'give-freely-root' in element_class

    def macro_text(self, attrs: dict[str, str]) -> str | None:
        if 'editor-inline-macro' not in attrs.get('class', ''):
            return None

        macro_name = attrs.get('data-macro-name', '').strip()
        if macro_name == 'toc':
            return None

        if macro_name == 'status':
            params = attrs.get('data-macro-parameters', '')
            match = re.search(r'(?:^|\|)title=([^|]+)', params)
            if match:
                return match.group(1).strip()
            default_param = attrs.get('data-macro-default-parameter', '').strip()
            return default_param or None

        if macro_name == 'jira':
            return '_Confluence jira macro omitted in saved HTML export._'

        return f'_Confluence {macro_name} macro omitted in saved HTML export._'

    def embedded_image_ref(self, attrs: dict[str, str]) -> ImageRef | None:
        if 'confluence-embedded-image' not in attrs.get('class', ''):
            return None

        source_path = attrs.get('src', '').strip()
        if not source_path:
            return None

        title = attrs.get('data-element-title') or attrs.get('data-linked-resource-default-alias') or attrs.get('title') or None
        return ImageRef(section_key=self.current_section_key(), source_path=source_path, title=title)

    def append_fragment(self, text: str) -> None:
        if not text:
            return

        if self.active_link is not None:
            self.active_link['text'].append(text)
            return

        target = self.current_target()
        if target is not None:
            target.append(text)
        else:
            self.orphan_fragments.append(text)

    def current_target(self) -> list[str] | None:
        if self.current_cell is not None:
            return self.current_cell
        if self.current_heading_level is not None:
            return self.current_heading_text
        if self.current_list_item is not None:
            return self.current_list_item
        if self.current_paragraph is not None:
            return self.current_paragraph
        return None

    def start_section(self, title: str, level: int) -> None:
        while len(self.heading_stack) >= level:
            self.heading_stack.pop()
        self.heading_stack.append(title)
        key = build_section_key(self.heading_stack)
        self.ensure_section(key, title)

    def ensure_section(self, key: str, title: str) -> dict[str, object]:
        if key not in self.sections:
            self.sections[key] = {
                'title': title,
                'blocks': [],
                'position': self.section_counter,
            }
            self.section_counter += 1
        return self.sections[key]

    def current_section(self) -> dict[str, object]:
        if not self.heading_stack:
            return self.ensure_section(DOCUMENT_PREAMBLE, DOCUMENT_PREAMBLE)

        key = build_section_key(self.heading_stack)
        return self.ensure_section(key, self.heading_stack[-1])

    def current_section_key(self) -> str:
        if not self.heading_stack:
            return DOCUMENT_PREAMBLE
        return build_section_key(self.heading_stack)

    def emit_block(self, text: str) -> None:
        block = normalize_block(text)
        if not block:
            return
        section = self.current_section()
        section['blocks'].append(block)

    def flush_orphan_fragments(self) -> None:
        if not self.orphan_fragments:
            return

        text = normalize_block(''.join(self.orphan_fragments))
        self.orphan_fragments = []
        if not text:
            return

        if text in IMPLICIT_HEADINGS:
            self.start_section(text, heading_level(text))
            return

        self.emit_block(text)

    def flush_paragraph(self) -> None:
        if self.current_paragraph is None:
            return
        self.emit_block(''.join(self.current_paragraph))
        self.current_paragraph = None

    def flush_lists(self) -> None:
        while self.list_stack:
            list_state = self.list_stack.pop()
            items = list_state['items']
            if not items:
                continue
            ordered = bool(list_state['ordered'])
            prefix = lambda index: f'{index}. ' if ordered else '- '
            block = '\n'.join(f'{prefix(index)}{item}' for index, item in enumerate(items, start=1))
            self.emit_block(block)

    def flush_table(self) -> None:
        if self.current_table is None:
            return
        table_markdown = render_table(self.current_table)
        if table_markdown:
            self.emit_block(table_markdown)
        self.current_table = None

    def finalize(self) -> tuple[dict[str, SectionBlock], list[ImageRef]]:
        self.flush_orphan_fragments()
        self.flush_paragraph()
        self.flush_lists()
        self.flush_table()

        sections: dict[str, SectionBlock] = {}
        for key, raw in self.sections.items():
            title = str(raw['title'])
            blocks = raw['blocks']
            content = '\n\n'.join(blocks)
            sections[key] = SectionBlock(
                key=key,
                title=title,
                content=content,
                position=int(raw['position']),
            )
        return sections, self.section_images


def render_table(rows: list[tuple[list[str], bool]]) -> str:
    cleaned_rows = [(cells, is_header) for cells, is_header in rows if any(cell.strip() for cell in cells)]
    if not cleaned_rows:
        return ''

    widths = [len(cells) for cells, _ in cleaned_rows]
    column_count = max(widths)

    normalized_rows: list[tuple[list[str], bool]] = []
    for cells, is_header in cleaned_rows:
        padded = cells + [''] * (column_count - len(cells))
        normalized_rows.append((padded, is_header))

    header_cells, header_is_header = normalized_rows[0]
    if not header_is_header:
        header_cells = [f'Column {index}' for index in range(1, column_count + 1)]
        body_rows = [cells for cells, _ in normalized_rows]
    else:
        body_rows = [cells for cells, _ in normalized_rows[1:]]

    header = '| ' + ' | '.join(escape_table_cell(cell) for cell in header_cells) + ' |'
    separator = '| ' + ' | '.join('---' for _ in range(column_count)) + ' |'
    lines = [header, separator]

    for cells in body_rows:
        lines.append('| ' + ' | '.join(escape_table_cell(cell) for cell in cells) + ' |')

    return '\n'.join(lines)


def escape_table_cell(text: str) -> str:
    return text.replace('|', '\\|')


def parse_html_sections(html_path: Path) -> tuple[dict[str, SectionBlock], list[ImageRef], str | None]:
    raw_html = html_path.read_text(encoding='utf-8')
    parser = ConfluenceHtmlSectionParser()
    parser.feed(raw_html)
    parser.close()
    sections, section_images = parser.finalize()
    return sections, section_images, extract_saved_from_url(raw_html)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('source_path', help='Path to a saved Confluence TDIA HTML file')
    parser.add_argument(
        '--section',
        action='append',
        dest='sections',
        help='Repeat to limit output to specific headings or section paths. Defaults to extracting the full TDIA.',
    )
    output_group = parser.add_mutually_exclusive_group()
    output_group.add_argument('--output', help='Write markdown output to a specific file')
    output_group.add_argument(
        '--output-root',
        help='Create `.codex-docs/tdia/<tdia-name>/` style output under this root with `source.html` and `extracted.md`.',
    )
    parser.add_argument(
        '--e2e-image',
        help='Optional path to the E2E interactions diagram image. When used with `--output-root`, the image is copied to `images/` beside `extracted.md`.',
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    source_path = Path(args.source_path).expanduser().resolve()

    if not source_path.exists():
        print(f'[ERROR] Source file not found: {source_path}', file=sys.stderr)
        return 1

    if args.e2e_image and not args.output_root:
        print('[ERROR] `--e2e-image` requires `--output-root` so the image can be copied beside `extracted.md`.', file=sys.stderr)
        return 1

    e2e_image_path: Path | None = None
    if args.e2e_image:
        e2e_image_path = Path(args.e2e_image).expanduser().resolve()
        if not e2e_image_path.exists():
            print(f'[ERROR] E2E image not found: {e2e_image_path}', file=sys.stderr)
            return 1

    suffix = source_path.suffix.lower()
    requested = resolve_requested_sections(args.sections)

    if suffix not in {'.html', '.htm'}:
        print(f'[ERROR] Unsupported file type: {source_path.suffix}', file=sys.stderr)
        print('[ERROR] Expected .html or .htm', file=sys.stderr)
        return 1

    sections, section_images, saved_from_url = parse_html_sections(source_path)
    e2e_image_markdown = None
    e2e_image_missing_note = None
    section_image_markdown: dict[str, list[str]] | None = None
    if e2e_image_path is not None:
        e2e_image_markdown = f'![E2E Interactions](images/e2e-interactions{e2e_image_path.suffix.lower()})'
    elif 'E2E Interactions' in sections and not any(ref.section_key == 'E2E Interactions' for ref in section_images):
        e2e_image_missing_note = '_E2E interactions image not provided. Ask the user for the diagram export and store it under `images/` beside this file._'

    if args.output_root:
        output_root = Path(args.output_root).expanduser().resolve()
        tdia_name = derive_tdia_name(sections, fallback=source_path.stem)
        target_dir = output_root / slugify(tdia_name)
        section_image_markdown = copy_section_images(
            target_dir=target_dir,
            html_path=source_path,
            image_refs=section_images,
            allowed_sections={'E2E Interactions'},
        )

    output = build_output(
        source_path=source_path,
        sections=sections,
        requested=requested,
        saved_from_url=saved_from_url,
        e2e_image_markdown=e2e_image_markdown,
        e2e_image_missing_note=e2e_image_missing_note,
        section_image_markdown=section_image_markdown,
    )

    if args.output_root:
        write_output_bundle(output_root, source_path, output, sections, e2e_image_path=e2e_image_path)
    elif args.output:
        output_path = Path(args.output).expanduser().resolve()
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(output, encoding='utf-8')
    else:
        sys.stdout.write(output)

    return 0


if __name__ == '__main__':
    raise SystemExit(main())

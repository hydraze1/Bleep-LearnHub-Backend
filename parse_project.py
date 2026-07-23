import os
import re

src_dir = 'src/main/java/com/bleep/learnhub/'
output_file = 'projectInfo/PROJECT_DESCRIPTION.md'

if not os.path.exists('projectInfo'):
    os.makedirs('projectInfo')

def get_files(directory):
    file_list = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.java'):
                file_list.append(os.path.join(root, file))
    return file_list

files = get_files(src_dir)

entities = []
controllers = []
services = []
repositories = []
dtos = []
configs = []
enums = []
security = []

for f in files:
    with open(f, 'r') as fp:
        content = fp.read()
        if 'entity' in f:
            if 'enum' in f:
                enums.append((f, content))
            else:
                entities.append((f, content))
        elif 'controller' in f:
            controllers.append((f, content))
        elif 'service' in f:
            services.append((f, content))
        elif 'repository' in f:
            repositories.append((f, content))
        elif 'dto' in f:
            dtos.append((f, content))
        elif 'config' in f:
            configs.append((f, content))
        elif 'security' in f:
            security.append((f, content))

with open(output_file, 'w') as out:
    out.write('# Bleep LearnHub Backend - Project Description\n\n')
    out.write('## Project Overview\n')
    out.write('This is a Spring Boot backend application for the Bleep LearnHub platform. It manages entities such as Students, Courses, Batches, Vendors, Partners, and handles enrollments, complaints, and user sessions. It provides a secure API for various frontends to interact with the system.\n\n')
    
    out.write('## Project Structure\n')
    out.write('The project follows a standard multi-layer Spring Boot architecture: Controllers, Services, Repositories, Entities (Models), DTOs, Security, and Configuration.\n\n')
    
    out.write('## Configurations\n')
    for f, c in configs:
        name = os.path.basename(f)
        out.write(f'### `{name}`\n')
        out.write('Handles configuration for ' + name.replace('Config.java', '').replace('.java', '') + '.\n\n')
    
    out.write('## Enums\n')
    for f, c in enums:
        name = os.path.basename(f)
        out.write(f'### `{name}`\n')
        vals = re.findall(r'(\w+)(?:,|\s*;)', c)
        vals = [v for v in vals if v.isupper()]
        out.write(f'Values: {", ".join(vals)}\n\n')

    out.write('## Entities (Database Tables)\n')
    for f, c in entities:
        name = os.path.basename(f).replace('.java', '')
        table_match = re.search(r'@Table\(name\s*=\s*"([^"]+)"\)', c)
        table_name = table_match.group(1) if table_match else name.lower()
        out.write(f'### `{name}` (Table: `{table_name}`)\n')
        fields = re.findall(r'private\s+([A-Za-z0-9<>_]+)\s+([A-Za-z0-9_]+);', c)
        if fields:
            out.write('Fields:\n')
            for t, n in fields:
                out.write(f'- `{n}` ({t})\n')
        out.write('\n')

    out.write('## DTOs (Request/Response)\n')
    for f, c in dtos:
        name = os.path.basename(f)
        out.write(f'- `{name}`\n')
    out.write('\n')

    out.write('## Repositories\n')
    for f, c in repositories:
        name = os.path.basename(f)
        out.write(f'- `{name}`: Interface for database operations related to the respective entity.\n')
    out.write('\n')

    out.write('## Services\n')
    for f, c in services:
        name = os.path.basename(f)
        out.write(f'- `{name}`: Contains business logic for the respective domain.\n')
    out.write('\n')
    
    out.write('## Controllers & APIs\n')
    for f, c in controllers:
        name = os.path.basename(f)
        out.write(f'### `{name}`\n')
        base_route = re.search(r'@RequestMapping\("([^"]+)"\)', c)
        if base_route:
            out.write(f'Base Route: `{base_route.group(1)}`\n')
        
        endpoints = re.findall(r'@(?:Get|Post|Put|Delete|Patch)Mapping\s*\(\s*(?:value\s*=\s*)?"([^"]*)"', c)
        for ep in endpoints:
            out.write(f'- Endpoint: `{ep}`\n')
        out.write('\n')


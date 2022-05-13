import React from 'react';
import {
    Progress,
    ProgressSize,
    ProgressMeasureLocation,
    ActionList,
    ActionListItem,
    Button,
    Dropdown,
    DropdownItem,
    DropdownSeparator,
    KebabToggle
} from '@patternfly/react-core';
import {
    TableComposable,
    Caption,
    Thead,
    Tr,
    Th,
    Tbody,
    Td
} from '@patternfly/react-table';

import {
    BrowserRouter as Router,
    Switch,
    Route,
    Link,
    useRouteMatch
} from "react-router-dom";

interface Repository {
    name: string;
    trial: number;
}

type ExampleType = 'default' | 'compact' | 'compactBorderless';

export const ExperimentsTable: React.FunctionComponent = () => {
    // In real usage, this data would come from some external source like an API via props.
    const repositories: Repository[] = [
        {name: 'techempower', trial: 1 },
        {name: 'local-test', trial: 58}
    ];

    const columnNames = {
        name: 'Name',
        trial: 'Progress',
        actions: 'Actions'
    };


    return (
        <React.Fragment>
            <TableComposable
                aria-label="Simple table"
                variant='compact'
                borders={true}
            >
                <Caption>Running Experiments</Caption>
                <Thead>
                    <Tr>
                        <Th width={25}>{columnNames.name}</Th>
                        <Th width={60}>{columnNames.trial}</Th>
                        <Th width={15}>{columnNames.actions}</Th>
                    </Tr>
                </Thead>
                <Tbody>
                    {repositories.map(repo => (
                        <Tr key={repo.name}>
                            <Td dataLabel={columnNames.name}>{repo.name}</Td>
                            <Td dataLabel={columnNames.trial}><Progress
                                measureLocation={ProgressMeasureLocation.outside} value={repo.trial}
                                size={ProgressSize.sm}/></Td>
                            <Td dataLabel={columnNames.actions} isActionCell={true}><ActionListSingleGroup/></Td>
                        </Tr>
                    ))}
                </Tbody>
            </TableComposable>
        </React.Fragment>
    );
};


const ActionListSingleGroup: React.FunctionComponent = () => {
    const [isOpen, setIsOpen] = React.useState(false);

    const onToggle = (
        isOpen: boolean,
        event: MouseEvent | TouchEvent | KeyboardEvent | React.KeyboardEvent<any> | React.MouseEvent<HTMLButtonElement>
    ) => {
        event.stopPropagation();
        setIsOpen(isOpen);
    };

    const onSelect = (event: React.SyntheticEvent<HTMLDivElement, Event>) => {
        event.stopPropagation();
        setIsOpen(!isOpen);
    };


    const dropdownItems = [
        <DropdownItem key="pause action" component="button">
            Pause
        </DropdownItem>,
        <DropdownItem key="cancel action" component="button">
            Cancel
        </DropdownItem>,
        <DropdownSeparator key="separator"/>,
        <DropdownItem key="separated link"><Link to={'/experiment'}>Edit</Link></DropdownItem>,
        // <DropdownItem key="separated link">Edit</DropdownItem>
    ];

    return (
        <React.Fragment>
            <ActionList>
                <ActionListItem>
                    <Dropdown
                        // onSelect={onSelect}
                        toggle={<KebabToggle onToggle={onToggle}/>}
                        isOpen={isOpen}
                        isPlain
                        dropdownItems={dropdownItems}
                        position="right"
                    />
                </ActionListItem>
            </ActionList>
        </React.Fragment>
    );
};

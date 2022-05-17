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
    KebabToggle, Bullseye, EmptyState, EmptyStateVariant, EmptyStateIcon, EmptyStateBody, Title, ProgressVariant
} from '@patternfly/react-core';
import {
    TableComposable,
    Thead,
    Tr,
    Th,
    Tbody,
    Td
} from '@patternfly/react-table';

import {Link, Route} from "react-router-dom";
import {SearchIcon} from "@patternfly/react-icons";
import {ExperimentDetails} from "@app/Experiments/components/ExperiementDetails";


type ExampleType = 'default' | 'compact' | 'compactBorderless';



interface IExperimentState {
    name: string ;
    state: string ;
}

interface IExperiment {
    experimentName: string;
    total_Trials: number;
    currentTrial: number;
    currentState: string;
}

export function ExperimentsTable() {

    const [experiments, setExperiments] = React.useState<IExperiment[]>([]);

    const columnNames = {
        name: 'Name',
        trial: 'Progress',
        actions: 'Actions'
    };

    let newExpRequest = new Request(
        "/api/hpo/experiment",
        {
            method: "get",
        }
    )

    fetch(newExpRequest)
        .then(res => res.json())
        .then(res => {
            setExperiments(res);
        })


    return (
        <TableComposable
            // aria-label="Simple table"
            variant='compact'
            borders={true}
            aria-label="Empty state table"
        >
            <Thead>
                <Tr>
                    <Th width={20}>{columnNames.name}</Th>
                    <Th width={70}>{columnNames.trial}</Th>
                    <Th width={10}>{columnNames.actions}</Th>
                </Tr>
            </Thead>
            <Tbody>
                <Tr hidden={!(experiments.length === 0)}>
                    <Td colSpan={8}>
                        <Bullseye>
                            <EmptyState variant={EmptyStateVariant.small}>
                                <EmptyStateIcon icon={SearchIcon} />
                                <Title headingLevel="h2" size="lg">
                                    No experiments found
                                </Title>
                                <EmptyStateBody>Please create a new Experiment and try again</EmptyStateBody>
                            </EmptyState>
                        </Bullseye>
                    </Td>
                </Tr>
                {experiments.map(experiment => (
                    <Tr key={experiment.experimentName}>
                        <Td dataLabel={columnNames.name}><Link to={'/experiment/'+experiment.experimentName}>{experiment.experimentName}</Link></Td>
                        <Td dataLabel={columnNames.trial}>
                            <Progress
                            measureLocation={ProgressMeasureLocation.outside} value={experiment.currentTrial / experiment.total_Trials * 100.0}
                            size={ProgressSize.md}
                            variant={experiment.currentState === 'RUNNING' ? ProgressVariant.success : ProgressVariant.warning}

                            /></Td>
                        <Td dataLabel={columnNames.actions}
                            isActionCell={true}><ActionListSingleGroup experimentName={experiment.experimentName}/></Td>
                    </Tr>
                ))}
            </Tbody>
        </TableComposable>
);
}


const ActionListSingleGroup = ({experimentName}) => {
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


        const startRun = async () => {
            // alert ('start: ' + experimentName)
            let state  = '{"name": "' + experimentName + '", "state": "RUNNING"};'

            let uppdateExpRequest = new Request(
                "/api/hpo/experiment/state",
                {
                    method: "put",
                    headers: {'Content-Type': 'application/json'},
                    body: state
                }
            )

            let response = await fetch(uppdateExpRequest);

            if (response.ok) {
                let data = response.json();
                // console.log(data);
            } else {
                let data = await response.json();
            }

        }

    const pauseRun = async () => {
        // alert ('start: ' + experimentName)
        let state  = '{"name": "' + experimentName + '", "state": "PAUSED"};'

        let uppdateExpRequest = new Request(
            "/api/hpo/experiment/state",
            {
                method: "put",
                headers: {'Content-Type': 'application/json'},
                body: state
            }
        )

        let response = await fetch(uppdateExpRequest);

        if (response.ok) {
            let data = response.json();
            // console.log(data);
        } else {
            let data = await response.json();
        }

    }

    const dropdownItems = [
            <DropdownItem key="run action" component="button" onClick={startRun}>
                Run
            </DropdownItem>,
            <DropdownItem key="pause action" component="button" onClick={pauseRun}>
                Pause
            </DropdownItem>,
            <DropdownItem key="cancel action" component="button">
                Cancel
            </DropdownItem>,
            <DropdownSeparator key="separator"/>,
            <DropdownItem key="separated link">
                <Link to={'/experiment/'+experimentName}>Edit</Link>
            </DropdownItem>,
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
